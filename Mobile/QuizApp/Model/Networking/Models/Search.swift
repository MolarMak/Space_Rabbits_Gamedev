//
//  Search.swift
//  QuizApp
//
//  Created by Alex on 28.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import Foundation

struct SearchResponse: Decodable {
    let roomId: String
}

extension EndpointCollection {
    
    static let search = Endpoint(method: .GET, pathEnding: "onlineGameRoom")
    
}
