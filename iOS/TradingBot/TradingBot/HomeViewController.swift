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
    
    override func viewDidLoad() {
        super.viewDidLoad()
        print(TradingBotSDKVersionNumber)
        
        print(getStockTickers())
    }
    
    func getStockTickers() -> [[String]]? {
        do {
            let path = Bundle.main.path(forResource: "stock_tickers", ofType: "csv")!
            let data = try Data(contentsOf: URL(fileURLWithPath: path))
            let dataEncoded = String(data: data, encoding: .utf8)
            
            let parsedCSV = dataEncoded?.components(separatedBy: "\n").map({
                
                $0.components(separatedBy: ",")
            })
             
            return parsedCSV ?? nil
        } catch {
            print(error.localizedDescription)
            return []
        }
    }
    
}

