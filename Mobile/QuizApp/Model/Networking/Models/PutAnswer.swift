//
//  PutAnswer.swift
//  QuizApp
//
//  Created by Alex on 28.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import Foundation

struct PutAnswerRequest: Encodable {
    let gameRoomId: String
    let answerNumber: Int // 0 - 4
    let answer: Int // 1 - right, 2 - false
}

extension EndpointCollection {
    
    static let putAnswer = Endpoint(method: .POST, pathEnding: "putAnswer")
    
}
