//
//  SearchVC.swift
//  QuizApp
//
//  Created by Alex on 21.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import UIKit

class SearchVC: UIViewController {

    var timer: Timer?
    var roomId: String?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        networking.performRequest(to: EndpointCollection.search) { [weak self] (result: Result<SearchResponse, Error>) in
            switch result {
            case .success(let response):
                self?.roomId = response.roomId
                DispatchQueue.main.async {
                    self?.timer = Timer.scheduledTimer(timeInterval: TimeInterval(2), target: self!, selector: #selector(self!.refresh), userInfo: nil, repeats: true)
                }
            case .failure(let error):
                DispatchQueue.main.async {
                    self?.show(error: error)
                }
            }
        }
    }
    
    @objc func refresh() {
        networking.performRequest(to: EndpointCollection.onlineGameRoomInfo(id: roomId ?? "")) { [weak self] (result: Result<GameRoomInfoResponse, Error>) in
            switch result {
            case .success(let response):
                if response.player1Name != nil && response.player2Name != nil {
                    DispatchQueue.main.async {
                        self?.proceed()
                    }
                }
            case .failure(let error):
                DispatchQueue.main.async {
                    self?.show(error: error)
                }
            }
        }
    }
    
    func proceed() {
        self.timer?.invalidate()
        self.timer = nil
        self.performSegue(withIdentifier: "game", sender: nil)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "game" {
            (segue.destination as? GameVC)?.roomId = self.roomId
        }
    }

}
