//
//  Stock.swift
//  TradingBot
//
//  Created by Bankbe on 2022/10/06.
//

import Foundation

class Stock {
    
    let name: String
    let df: [String: String]
    let buyScore: Int
    let strategyYield: Int
    let buyAndHoldYield: Int
    let winRate: Int
    
    
    init(name: String, df: [String: String]) {
        self.name = name
        self.df = df
        self.buyScore = -1
        self.strategyYield = -1
        self.buyAndHoldYield = -1
        self.winRate = -1
    }
}
