//
//  TradingBot.swift
//  TradingBotSDK
//
//  Created by Bankbe on 2022/08/31.
//

import Foundation

public class TradingBot {
    
    public var etfArr: [[String]] = []
    
    public init() {
    }
    
    public func setup() {
        print("TradingBot")
    }
    
    public func loadCSV() {
        let path = Bundle.main.path(forResource: "data/ETFs", ofType: "csv")!
        parseCSVAt(url: URL(fileURLWithPath: path))
        
        print(etfArr)
    }
    
    public func parseCSVAt(url: URL) {
        do {
            let data = try Data(contentsOf: url)
            let dataEncoded = String(data: data, encoding: .utf8)
            
            if let dataArr = dataEncoded?.components(separatedBy: "\n").map({ $0.components(separatedBy: ",") }) {
                for item in dataArr {
                    etfArr.append(item)
                }
            }
            
        } catch {
            print("Error reading CSV Files")
        }
    }
}
