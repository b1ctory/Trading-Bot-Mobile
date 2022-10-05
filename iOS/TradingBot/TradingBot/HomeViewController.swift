//
//  ViewController.swift
//  TradingBot
//
//  Created by exception on 2022/08/20.
//

import UIKit
import TradingBotSDK
import SnapKit
import PythonKit

class HomeViewController: UIViewController {
    var stockTickers: [String] = [String]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        print(TradingBotSDKVersionNumber)
        
        print(getStockTickers())
    }
    
    func getStockTickers() -> [String]? {
        do {
            let path = Bundle.main.path(forResource: "stock_tickers", ofType: "csv")!
            let data = try Data(contentsOf: URL(fileURLWithPath: path))
            let dataEncoded = String(data: data, encoding: .utf8)
            
            
            if let parsedCSV = dataEncoded?.components(separatedBy: "\n").map({
                $0.components(separatedBy: ",")
            }) {
                for i in 0..<(parsedCSV.count - 1) {
                    stockTickers.append(parsedCSV[i][1])
                }
                stockTickers.remove(at: 0)
                return stockTickers
            }
            return stockTickers ?? nil
        } catch {
            print(error.localizedDescription)
            return []
        }
    }
    
}

