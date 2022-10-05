//
//  ViewController.swift
//  TradingBot
//
//  Created by exception on 2022/08/20.
//

import UIKit
import SnapKit
import PythonKit

class HomeViewController: UIViewController {
    
    var stockTickers: [String]? = []
    var etfTickers: [String]? = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        stockTickers = getStockTickers()
        etfTickers = getETFTickers()
        
        print(stockTickers!)
        print(etfTickers!)
    }
    
    func getStockTickers() -> [String]? {
        do {
            var tickers: [String] = [String]()
            let path = Bundle.main.path(forResource: "stock_tickers", ofType: "csv")!
            let data = try Data(contentsOf: URL(fileURLWithPath: path))
            let dataEncoded = String(data: data, encoding: .utf8)
            
            
            if let parsedCSV = dataEncoded?.components(separatedBy: "\n").map({
                $0.components(separatedBy: ",")
            }) {
                for i in 0..<(parsedCSV.count - 1) {
                    tickers.append(parsedCSV[i][1])
                }
                tickers.remove(at: 0)
            }
            return tickers
        } catch {
            print(error.localizedDescription)
            return []
        }
    }
    
    func getETFTickers() -> [String]? {
        do {
            var tickers: [String] = [String]()
            let path = Bundle.main.path(forResource: "ETFs", ofType: "csv")!
            let data = try Data(contentsOf: URL(fileURLWithPath: path))
            let dataEncoded = String(data: data, encoding: .utf8)
            
            if let parsedCSV = dataEncoded?.components(separatedBy: "\n").map({
                $0.components(separatedBy: ",")
            }) {
                for i in 0..<(parsedCSV.count - 1) {
                    tickers.append(parsedCSV[i][0].replacingOccurrences(of: "\"", with: ""))
                }
                tickers.remove(at: 0)
            }
            
            return tickers
        } catch {
            print(error.localizedDescription)
            return []
        }
    }
    
}

