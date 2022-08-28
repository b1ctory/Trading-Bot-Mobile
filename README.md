# Trading-Bot-Mobile

#### Python 주가 예측 코드

**this_integer님 :** [GitHub - iinteger/Trading-Bot: 가상화폐, 주식 봇 개발기](https://github.com/iinteger/Trading-Bot)

#### Python 코드를 안드로이드에 적용하기

- **build.gradle (Project) 에 아래의 항목 추가**
  
  ```groovy
  buildscript {
      repositories {
          ...
          maven { url "https://chaquo.com/maven" }
      }
      dependencies {
          ...
          classpath "com.chaquo.python:gradle:12.0.1"
      }
  }
  ```

- **build.gradle (:app)에 아래의 항목 추가**
  
  ```groovy
  plugins {
      ...
      id 'com.chaquo.python'
  }
  ```

  android {
      ...

      defaultConfig {
          ...
    
          python {
              python {
                  buildPython "/usr/local/bin/python3"
                  pip {
                      install "numpy"
                      install "pandas"
                      install "pandas_datareader"
                      install "finance-datareader"
                      install "tqdm"
                      install "TA-Lib"
                      install "bs4"
                      install "yfinance"
                      install "simplejson"
                  }
              }
              sourceSets {
                  main {
                      python.srcDirs = ["src/main/python"]
                  }
              }
          }
    
          // 필요에 따라 추가 가능
          ndk {
              abiFilters "arm64-v8a"
          }
    
          ...
      }

  }

```
- **app/src/main/ 경로에 python directory 생성 후, 필요한 .py 파일과 .csv 파일을 넣어준다.**

      ![](/Users/cheonsuebin/Library/Application%20Support/marktext/images/2022-08-23-11-31-54-image.png)

- **main.py 파일에서 csv 파일을 정상적으로 불러오게 하기 위해 아래처럼 바꾸어 준다.**

```python
etf_file = join(dirname(__file__), "ETFs.csv")
etf_tickers = pd.read_csv(etf_file)
stock_file = join(dirname(__file__), "stock_tickers.csv")
stock_tickers = pd.read_csv(stock_file)
```

- **현재는 우선 run() 에서 주식, ETF를 모두 처리하지만, 해당 부분을 메소드 분리하여 처리할 예정**

- **MainActivity.kt에서는 아래와 같이 호출해준다.**
  
  ```kotlin
          try {
              if (!Python.isStarted()) {
                  Python.start(AndroidPlatform(this))
              }
  
              var py: Python = Python.getInstance()
              var pyObject: PyObject = py.getModule("main")
              pyObject.callAttr("run") // main.py의 run 메소드 호출
          } catch(e: PyException) {
              Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
              Log.e("Python", e.message.toString())
          }
  ```

```
- 이외에도 파이썬 코드를 .kt 파일 내에서 작성해서 .py 에 매개변수로 넘겨주는 방법 등도 지원함.

#### Python method

```python
# ETF 종목 모두 탐색하여 매수 종목 추출
def buyETF():


# 매수하고싶은 ETF 종목 한가지만 검색
def buyOneETF(ticker):


# 주식 종목 (나스닥 TOP 100) 모두 탐색하여 매수 종목 추출
def buyStock():


# 매수하고싶은 주식 종목 한가지만 검색 
def buyOneStock(ticker):


# tickers = ["ADBE", "XLV", "QCOM", "MDLZ", "IAU"]
# 매도하고 싶은 종목 Array 를 넣으면 매도 지수 도출
def sell(tickers):
```

- **[2022.08.28] 수정 필요한 내역**
  
  - 실행 시간 단축 (현재 약 5분 정도 소요되는데, 순수 파이썬 코드에 적용할 때보다 약 4배 이상 더 소요되는 것으로 추정)
  
  - progressbar 이 멈춰있는 현상
  
  - 단일 종목 검색
  
  - class로 수정된 Python 파일 새로 import 필요



#### [ Reference ]

https://www.wenyanet.com/opensource/ko/6116a630374d544626477a07.html

[Chaquopy 12.0](https://chaquo.com/chaquopy/doc/current/index.html)

[How to use Chaquopy to run Python Code and obtain its output using Java in your Android App | Our Code World](https://ourcodeworld.com/articles/read/1656/how-to-use-chaquopy-to-run-python-code-and-obtain-its-output-using-java-in-your-android-app)

[chaquopy-mishkal/MainActivity.kt at main · naskio/chaquopy-mishkal · GitHub](https://github.com/naskio/chaquopy-mishkal/blob/main/app/src/main/java/io/nask/mishkalandroid/MainActivity.kt)

[Reading/Loading a file · Issue #144 · chaquo/chaquopy · GitHub](https://github.com/chaquo/chaquopy/issues/144)
