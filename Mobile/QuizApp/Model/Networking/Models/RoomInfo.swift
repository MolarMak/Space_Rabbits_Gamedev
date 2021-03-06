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
    let aNswersPlayer1List: [Int] // 0 - unanswered, 1 - true answer, 2 - false answer
    let aNswersPlayer2List: [Int]
    
    enum CodingKeys: String, CodingKey {
        case gameRoomId
        case player1Name
        case player2Name
        case questionsList
        case aNswersPlayer1List = "answersPlayer1List"
        case aNswersPlayer2List = "answersPlayer2List"
    }
}

extension GameRoomInfoResponse {
    
    var answersPlayer1List: [Int] {
        if player1Name == "someuser" {
            return aNswersPlayer1List
        } else {
            return aNswersPlayer2List
        }
    }
    var answersPlayer2List: [Int] {
        if player2Name == "someuser" {
            return aNswersPlayer1List
        } else {
            return aNswersPlayer2List
        }
    }
    
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
