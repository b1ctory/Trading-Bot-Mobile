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

class Stock:
    def __init__(self, name, df):
        self.name = name
        self.df = df
        self.buy_score = -1
        self.strategy_yield = -1
        self.buy_and_hold_yield = -1
        self.win_rate = -1

    def get_indicator(self):
        close = self.df["Adj Close"]

        # 볼린저밴드
        self.df["Upper"], self.df["Middle"], self.df["Lower"] = talib.BBANDS(close, timeperiod=20)

        # 모멘텀
        self.df["Momentum"] = talib.MOM(close, timeperiod=10)
        self.df["Momentum_signal"] = talib.SMA(self.df["Momentum"], timeperiod=9)

        # 이평선
        self.df["MA5"] = talib.SMA(close, timeperiod=5)
        self.df["MA15"] = talib.SMA(close, timeperiod=20)
        self.df["MA20"] = talib.SMA(close, timeperiod=20)
        self.df["MA60"] = talib.SMA(close, timeperiod=60)

        # RSI
        self.df["RSI"] = talib.RSI(close, timeperiod=14)
        self.df["RSI_signal"] = talib.SMA(self.df["RSI"], timeperiod=6)

        # MACD
        self.df["MACD"], self.df["MACD_signal"], self.df["MACD_hist"] = talib.MACD(close)

        if len(self.df) > 504:
            self.df = self.df[-504:]

    def simulation(self):
        seed = 1000000
        buy_price = 0
        holding = False
        fee = 0.0016
        buy_threshold = 3
        sell_threshold = -4

        temp_df = self.df.dropna()
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
            if (temp_dict["Momentum"][i-1] < temp_dict["Momentum_signal"][i-1]) and (temp_dict["Momentum_signal"][i] < temp_dict["Momentum"][i]):
                temp_dict["Score"][i] += 2
            # 모멘텀이 모멘텀 시그널을 하향돌파시 -1
            elif (temp_dict["Momentum"][i-1] > temp_dict["Momentum_signal"][i-1]) and (temp_dict["Momentum_signal"][i] > temp_dict["Momentum"][i]):
                temp_dict["Score"][i] -= 2

            # 단기 이평선이 장기 이평선을 상향돌파시 +1
            if (temp_dict["MA15"][i-1] < temp_dict["MA20"][i-1]) and (temp_dict["MA20"][i] < temp_dict["MA15"][i]):
                temp_dict["Score"][i] += 1
            # 단기 이평선이 장기 이평선을 하향돌파시 -1
            elif (temp_dict["MA15"][i-1] > temp_dict["MA20"][i-1]) and (temp_dict["MA20"][i] > temp_dict["MA15"][i]):
                temp_dict["Score"][i] -= 1

            # RSI가 70을 넘어가면 과매수 -1
            if temp_dict["RSI"][i] >= 70:
                temp_dict["Score"][i] -= 2
            # RSI가 30에서 내려가면 과매도 +1
            elif temp_dict["RSI"][i] <= 30:
                temp_dict["Score"][i] += 2

            # RSI가 RSI 시그널을 상향돌파시 +1
            if (temp_dict["RSI"][i-1] < temp_dict["RSI_signal"][i-1]) and (temp_dict["RSI_signal"][i] < temp_dict["RSI"][i]):
                temp_dict["Score"][i] += 1
            # RSI가 RSI 시그널을 하향돌파시 -1
            elif (temp_dict["RSI"][i-1] > temp_dict["RSI_signal"][i-1]) and (temp_dict["RSI_signal"][i] > temp_dict["RSI"][i]):
                temp_dict["Score"][i] -= 1

            # MACD가 MACD 시그널을 상향돌파시 +1
            if (temp_dict["MACD"][i-1] < temp_dict["MACD_signal"][i-1]) and (temp_dict["MACD_signal"][i] < temp_dict["MACD"][i]):
                temp_dict["Score"][i] += 1
            # MACD가 MACD 시그널을 하향돌파시 -1
            elif (temp_dict["MACD"][i-1] > temp_dict["MACD_signal"][i-1]) and (temp_dict["MACD_signal"][i] > temp_dict["MACD"][i]):
                temp_dict["Score"][i] -= 1

        self.df = pd.DataFrame.from_dict(temp_dict)

        for index, row in self.df.iterrows():
            self.df.loc[index, "yield"] = int((seed/1000000-1)*100)
            if row["Score"] >= buy_threshold:
                if not holding:
                    buy_price = row["Adj Close"]
                    self.df.loc[index, "trade"] = "BUY"
                    holding = True

            elif row["Score"] <= sell_threshold:
                if holding:
                    sell_price = row["Adj Close"]
                    holding = False
                    if sell_price > buy_price:
                        self.df.loc[index, "trade"] = "SELL"
                    else:
                        self.df.loc[index, "trade"] = "STOP"
                    seed = seed * (sell_price/buy_price) * (1-fee)

            # 5%이상 손실날 때 손절
            elif holding and row["Adj Close"]/buy_price <= 0.9:
                sell_price = row["Adj Close"]
                holding = False
                self.df.loc[index, "trade"] = "STOP"
                seed = seed * (sell_price/buy_price) * (1-fee)

        if len(self.df[self.df["trade"]=="BUY"]) != len(self.df[self.df["trade"]=="SELL"]) + len(self.df[self.df["trade"]=="STOP"]):
            win_rate = len(self.df[self.df["trade"]=="SELL"])/(len(self.df[self.df["trade"]=="BUY"])-1)
        else:
            win_rate = len(self.df[self.df["trade"]=="SELL"])/len(self.df[self.df["trade"]=="BUY"])

        self.df["buy_and_hold"] = round(((1000000/self.df.iloc[0]["Adj Close"]*self.df["Adj Close"])/1000000-1)*100, 2)

        self.buy_score = self.df.iloc[-1]["Score"]
        self.strategy_yield = (seed/1000000-1)*100
        self.buy_and_hold_yield = self.df.iloc[-1]['buy_and_hold']
        self.win_rate = win_rate


def stock_to_string(self):
    return self.name + " " + str(self.buy_score) + " " + str(self.strategy_yield) + " " + str(self.buy_and_hold_yield) + " " + str(self.win_rate)

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


# ETF 전체 조회
def buyETF():
    print("------------ETF-----------")
    etf = []
    for ticker in tqdm(etf_tickers["Symbol"]):
        try:
            stock_df = pdr.get_data_yahoo(ticker)
            stock = Stock(ticker, stock_df)
            stock.get_indicator()
            stock.simulation()

            if stock.buy_score >= 3:
                etf.append(stock_to_string(stock))

            print(etf)

        except Exception as e:
            print("except")
            print(e)
            pass

    return etf

# ETF 단일 종목 조회
def buyOneETF(ticker):
    print("------------Buy One ETF-----------")
    try:
        stock_df = pdr.get_data_yahoo(ticker)
        stock = Stock(ticker, stock_df)
        stock.get_indicator()

        return stock.simulation()
    except Exception as e:
        print("except : " + ticker)
        print(e)
        pass

# 주식 전체 조회
def buyStock():
    print("------------주식-----------")
    stocks = []
    for ticker in tqdm(stock_tickers["Symbol"][:100]):
        try:
            stock_df = pdr.get_data_yahoo(ticker)
            stock = Stock(ticker, stock_df)
            stock.get_indicator()
            stock.simulation()

            if stock.buy_score >= 3:
                print(stock)
                stocks.append(stock_to_string(stock))

        except Exception as e:
            print("except")
            print(e)
            pass
    print(stocks)
    return stocks

# 주식 단일 종목 조회
def buyOneStock(ticker):
    print("------------주식 하나만-----------")

    # 주식
    try:
        stock_df = pdr.get_data_yahoo(ticker)
        stock = Stock(ticker, stock_df)
        stock.get_indicator()

        return stock.simulation()

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
