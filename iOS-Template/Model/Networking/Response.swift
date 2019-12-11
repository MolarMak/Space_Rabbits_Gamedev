//
//  Response.swift
//  iOS-Template
//
//  Created by Alex on 11.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import Foundation

struct Response<T: Decodable>: Decodable {
    let data: T?
    let result: Bool
    let errors: [String]?
}

struct Empty: Codable { }
