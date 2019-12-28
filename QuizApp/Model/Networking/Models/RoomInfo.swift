//
//  RoomInfo.swift
//  QuizApp
//
//  Created by Alex on 28.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import Foundation

struct GameRoomInfoResponse: Decodable {
    let gameRoomId: String
    let player1Name: String?
    let player2Name: String?
    let questionsList: [GameRoomInfoQuestion]
    let answersPlayer1List: [Int] // 0 - unanswered, 1 - true answer, 2 - false answer
    let answersPlayer2List: [Int]
}

struct GameRoomInfoQuestion: Decodable {
    let fact: FactEntityResponse
    let useTrueQuestion: Bool
}

extension EndpointCollection {
    
    static func onlineGameRoomInfo(id: String) -> Endpoint {
        return Endpoint(method: .GET, pathEnding: "onlineGameRoomInfo?gameRoomId=" + id)
    }
    
}
