import pandas as pd
from pandas_datareader import data as pdr
import FinanceDataReader as fdr
import yfinance
from tqdm import tqdm
import time
import warnings
import talib
from os.path import dirname, join
import time

warnings.filterwarnings("ignore")

etf_file = join(dirname(__file__), "ETFs.csv")
etf_tickers = pd.read_csv(etf_file)
stock_file = join(dirname(__file__), "stock_tickers.csv")
stock_tickers = pd.read_csv(stock_file)


def get_buy_position(df):
    close = df["Adj Close"]

    # 볼린저밴드
    df["Upper"], df["Middle"], df["Lower"] = talib.BBANDS(close, timeperiod=20)

    # 모멘텀
    df["Momentum"] = talib.MOM(close, timeperiod=10)
    df["Momentum_signal"] = talib.SMA(df["Momentum"], timeperiod=9)

    # 이평선
    df["MA5"] = talib.SMA(close, timeperiod=5)
    df["MA15"] = talib.SMA(close, timeperiod=20)
    df["MA20"] = talib.SMA(close, timeperiod=20)
    df["MA60"] = talib.SMA(close, timeperiod=60)

    # RSI
    df["RSI"] = talib.RSI(close, timeperiod=14)
    df["RSI_signal"] = talib.SMA(df["RSI"], timeperiod=6)

    # MACD
    df["MACD"], df["MACD_signal"], df["MACD_hist"] = talib.MACD(close)

    return df


def simulation(ticker, df):
    seed = 1000000
    buy_price = 0
    holding = False
    fee = 0.0016
    buy_threshold = 3
    sell_threshold = -4

    temp_df = df.dropna()
    temp_df = temp_df.reset_index()
    temp_df["Score"] = 0
    temp_dict = dict(temp_df)

    for i in range(1, len(temp_dict["Close"])):
        # 주가가 밴드 상단보다 높으면 -1
        if temp_dict["Adj Close"][i] > temp_dict["Upper"][i]:
            temp_dict["Score"][i] -= 2
        # 주가가 밴드 하단보다 낮으면 +1
        elif temp_dict["Adj Close"][i] < temp_dict["Lower"][i]:
            temp_dict["Score"][i] += 2

        # 모멘텀이 0보다 위일 때 +1
        if temp_dict["Momentum"][i] > 0:
            temp_dict["Score"][i] += 1
        # 모멘텀이 0보다 아래일 때 -1
        elif temp_dict["Momentum"][i] < 0:
            temp_dict["Score"][i] -= 1

        # 모멘텀이 모멘텀 시그널을 상향돌파시 +1
        if (temp_dict["Momentum"][i - 1] < temp_dict["Momentum_signal"][i - 1]) and (
                temp_dict["Momentum_signal"][i] < temp_dict["Momentum"][i]):
            temp_dict["Score"][i] += 2
        # 모멘텀이 모멘텀 시그널을 하향돌파시 -1
        elif (temp_dict["Momentum"][i - 1] > temp_dict["Momentum_signal"][i - 1]) and (
                temp_dict["Momentum_signal"][i] > temp_dict["Momentum"][i]):
            temp_dict["Score"][i] -= 2

        # 단기 이평선이 장기 이평선을 상향돌파시 +1
        if (temp_dict["MA15"][i - 1] < temp_dict["MA20"][i - 1]) and (
                temp_dict["MA20"][i] < temp_dict["MA15"][i]):
            temp_dict["Score"][i] += 1
        # 단기 이평선이 장기 이평선을 하향돌파시 -1
        elif (temp_dict["MA15"][i - 1] > temp_dict["MA20"][i - 1]) and (
                temp_dict["MA20"][i] > temp_dict["MA15"][i]):
            temp_dict["Score"][i] -= 1

        # RSI가 70을 넘어가면 과매수 -1
        if temp_dict["RSI"][i] >= 70:
            temp_dict["Score"][i] -= 2
        # RSI가 30에서 내려가면 과매도 +1
        elif temp_dict["RSI"][i] <= 30:
            temp_dict["Score"][i] += 2

        # RSI가 RSI 시그널을 상향돌파시 +1
        if (temp_dict["RSI"][i - 1] < temp_dict["RSI_signal"][i - 1]) and (
                temp_dict["RSI_signal"][i] < temp_dict["RSI"][i]):
            temp_dict["Score"][i] += 1
        # RSI가 RSI 시그널을 하향돌파시 -1
        elif (temp_dict["RSI"][i - 1] > temp_dict["RSI_signal"][i - 1]) and (
                temp_dict["RSI_signal"][i] > temp_dict["RSI"][i]):
            temp_dict["Score"][i] -= 1

        # MACD가 MACD 시그널을 상향돌파시 +1
        if (temp_dict["MACD"][i - 1] < temp_dict["MACD_signal"][i - 1]) and (
                temp_dict["MACD_signal"][i] < temp_dict["MACD"][i]):
            temp_dict["Score"][i] += 1
        # MACD가 MACD 시그널을 하향돌파시 -1
        elif (temp_dict["MACD"][i - 1] > temp_dict["MACD_signal"][i - 1]) and (
                temp_dict["MACD_signal"][i] > temp_dict["MACD"][i]):
            temp_dict["Score"][i] -= 1

    df = pd.DataFrame.from_dict(temp_dict)

    for index, row in df.iterrows():
        df.loc[index, "yield"] = int((seed / 1000000 - 1) * 100)
        if row["Score"] >= buy_threshold:
            if not holding:
                buy_price = row["Adj Close"]
                df.loc[index, "trade"] = "BUY"
                holding = True

        elif row["Score"] <= sell_threshold:
            if holding:
                sell_price = row["Adj Close"]
                holding = False
                if sell_price > buy_price:
                    df.loc[index, "trade"] = "SELL"
                else:
                    df.loc[index, "trade"] = "STOP"
                seed = seed * (sell_price / buy_price) * (1 - fee)

        # 5%이상 손실날 때 손절
        elif holding and row["Adj Close"] / buy_price <= 0.9:
            sell_price = row["Adj Close"]
            holding = False
            df.loc[index, "trade"] = "STOP"
            seed = seed * (sell_price / buy_price) * (1 - fee)

    if df.iloc[-1]["Score"] >= buy_threshold:
        if len(df[df["trade"] == "BUY"]) != len(df[df["trade"] == "SELL"]) + len(
                df[df["trade"] == "STOP"]):
            win_rate = len(df[df["trade"] == "SELL"]) / (len(df[df["trade"] == "BUY"]) - 1)
        else:
            win_rate = len(df[df["trade"] == "SELL"]) / len(df[df["trade"] == "BUY"])

        # if win_rate <= 0.5 or round((seed / 1000000 - 1) * 100, 2) < 0:
        #     return

        temp_dict = dict()
        temp_dict["ticker"] = ticker
        temp_dict["buy score"] = df.iloc[-1]["Score"]
        temp_dict["전략 수익률"] = round((seed / 1000000 - 1) * 100, 2)
        df["buynhold"] = round(((1000000 / df.iloc[0]["Adj Close"] * df["Adj Close"]) / 1000000 - 1) * 100, 2)
        temp_dict["바이앤홀드 수익률"] = df.iloc[-1]['buynhold']
        temp_dict["승률"] = win_rate

        return temp_dict
    else:  # 살 종목 아닐때
        return None


def get_indicator(df):
    close = df["Adj Close"]

    # 볼린저밴드
    df["Upper"], df["Middle"], df["Lower"] = talib.BBANDS(close, timeperiod=20)

    # 모멘텀
    df["Momentum"] = talib.MOM(close, timeperiod=10)
    df["Momentum_signal"] = talib.SMA(df["Momentum"], timeperiod=9)

    # 이평선
    df["MA5"] = talib.SMA(close, timeperiod=5)
    df["MA15"] = talib.SMA(close, timeperiod=20)
    df["MA20"] = talib.SMA(close, timeperiod=20)
    df["MA60"] = talib.SMA(close, timeperiod=60)

    # RSI
    df["RSI"] = talib.RSI(close, timeperiod=14)
    df["RSI_signal"] = talib.SMA(df["RSI"], timeperiod=6)

    # MACD
    df["MACD"], df["MACD_signal"], df["MACD_hist"] = talib.MACD(close)

    return df


def get_sell_position(df, sell_threshold, ticker):
    df["Score"] = 0
    temp_dict = dict(df)

    # 주가가 밴드 상단보다 높으면 -1
    if temp_dict["Adj Close"][-1] > temp_dict["Upper"][-1]:
        temp_dict["Score"][-1] -= 2
    # 주가가 밴드 하단보다 낮으면 +1
    elif temp_dict["Adj Close"][-1] < temp_dict["Lower"][-1]:
        temp_dict["Score"][-1] += 2

    # 모멘텀이 0보다 위일 때 +1
    if temp_dict["Momentum"][-1] > 0:
        temp_dict["Score"][-1] += 1
    # 모멘텀이 0보다 아래일 때 -1
    elif temp_dict["Momentum"][-1] < 0:
        temp_dict["Score"][-1] -= 1

    # 모멘텀이 모멘텀 시그널을 상향돌파시 +1
    if (temp_dict["Momentum"][-2] < temp_dict["Momentum_signal"][-2]) and (
            temp_dict["Momentum_signal"][-1] < temp_dict["Momentum"][-1]):
        temp_dict["Score"][-1] += 2
    # 모멘텀이 모멘텀 시그널을 하향돌파시 -1
    elif (temp_dict["Momentum"][-2] > temp_dict["Momentum_signal"][-2]) and (
            temp_dict["Momentum_signal"][-1] > temp_dict["Momentum"][-1]):
        temp_dict["Score"][-1] -= 2

    # 단기 이평선이 장기 이평선을 상향돌파시 +1
    if (temp_dict["MA15"][-2] < temp_dict["MA20"][-2]) and (
            temp_dict["MA20"][-1] < temp_dict["MA15"][-1]):
        temp_dict["Score"][-1] += 1
    # 단기 이평선이 장기 이평선을 하향돌파시 -1
    elif (temp_dict["MA15"][-2] > temp_dict["MA20"][-2]) and (
            temp_dict["MA20"][-1] > temp_dict["MA15"][-1]):
        temp_dict["Score"][-1] -= 1

    # RSI가 70을 넘어가면 과매수 -1
    if temp_dict["RSI"][-1] >= 70:
        temp_dict["Score"][-1] -= 2
    # RSI가 30에서 내려가면 과매도 +1
    elif temp_dict["RSI"][-1] <= 30:
        temp_dict["Score"][-1] += 2

    # RSI가 RSI 시그널을 상향돌파시 +1
    if (temp_dict["RSI"][-2] < temp_dict["RSI_signal"][-2]) and (
            temp_dict["RSI_signal"][-1] < temp_dict["RSI"][-1]):
        temp_dict["Score"][-1] += 1
    # RSI가 RSI 시그널을 하향돌파시 -1
    elif (temp_dict["RSI"][-2] > temp_dict["RSI_signal"][-2]) and (
            temp_dict["RSI_signal"][-1] > temp_dict["RSI"][-1]):
        temp_dict["Score"][-1] -= 1

    # MACD가 MACD 시그널을 상향돌파시 +1
    if (temp_dict["MACD"][-2] < temp_dict["MACD_signal"][-2]) and (
            temp_dict["MACD_signal"][-1] < temp_dict["MACD"][-1]):
        temp_dict["Score"][-1] += 1
    # MACD가 MACD 시그널을 하향돌파시 -1
    elif (temp_dict["MACD"][-2] > temp_dict["MACD_signal"][-2]) and (
            temp_dict["MACD_signal"][-1] > temp_dict["MACD"][-1]):
        temp_dict["Score"][-1] -= 1

    df = pd.DataFrame.from_dict(temp_dict)

    temp_dict = dict()
    temp_dict["ticker"] = ticker
    temp_dict["현재 가격"] = df.iloc[-1]["Adj Close"]
    temp_dict["매도 스코어"] = df.iloc[-1]["Score"]
    is_sell = True if df.iloc[-1]["Score"] <= sell_threshold else False
    temp_dict["매도 여부"] = is_sell

    return temp_dict


def buyETF():
    # etf
    print("------------ETF-----------")
    etf = []
    for ticker in tqdm(etf_tickers["Symbol"]):
        try:
            df = pdr.get_data_yahoo(ticker)
            df = get_buy_position(df)
            if simulation(ticker, df) is not None:
                etf.append(simulation(ticker, df))

        except Exception as e:
            print("except : " + ticker)
            print(e)
            pass

    return etf


def buyOneETF(ticker):
    # etf
    print("------------Buy One ETF-----------")
    try:
        df = pdr.get_data_yahoo(ticker)
        df = get_buy_position(df)

        return simulation(ticker, df)
    except Exception as e:
        print("except : " + ticker)
        print(e)
        pass


def buyStock():
    print("------------주식-----------")
    # #주식
    stock = []
    for ticker in tqdm(stock_tickers["Symbol"][:100]):
        try:
            df = pdr.get_data_yahoo(ticker)
            df = get_buy_position(df)
            if simulation(ticker, df) is not None:
                stock.append(simulation(ticker, df))
            time.sleep(0.5)
            print(stock)
        except Exception as e:
            print("except : " + ticker)
            print(e)
            pass

    return stock


def buyOneStock(ticker):
    print("------------주식 하나만-----------")

    # 주식
    try:
        df = pdr.get_data_yahoo(ticker)
        df = get_buy_position(df)
        return simulation(ticker, df)

    except Exception as e:
        print("except : " + ticker)
        print(e)
        pass


# tickers = ["ADBE", "XLV", "QCOM", "MDLZ", "IAU"]
def sell(tickers):
    sell_threshold = -4
    print(f"현재 스코어가 {sell_threshold} 이하일 때 판매\n")
    sell_result = []
    for ticker in tickers:
        time.sleep(0.5)
        df = pdr.get_data_yahoo(ticker, "2022")
        df = get_indicator(df)
        get_sell_position(df, sell_threshold, ticker)
        sell_result.append(ticker)

    return sell_result
