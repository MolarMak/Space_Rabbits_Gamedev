//
//  FactsStore.swift
//  iOS-Template
//
//  Created by Alex on 16.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import Foundation

class FactsStore {
    
    static let shared = FactsStore()
    private init() {
        facts = database.fetchAll()
    }
    
    private var facts = [Fact]()
        
    // MARK: - Fact updates
    
    func runFactsUpdate(progressHandler progress: @escaping (Float) -> Void, completionHandler completion: @escaping () -> Void) {
        let currentVersion = 0
        
        updateFacts(currentVersion: currentVersion, offset: 0, limit: 3, progressHandler: progress, completionHandler: completion)
    }
    
    private func updateFacts(currentVersion: Int, offset: Int, limit: Int, progressHandler progress: @escaping (Float) -> Void, completionHandler completion: @escaping () -> Void) {
        networking.performRequest(to: EndpointCollection.syncFacts(version: currentVersion, offset: offset, limit: limit)) { [weak self] (result: Result<SyncFactsResponse, Error>) in
            switch result {
            case .success(let response):
                response.facts.forEach { (factEntity) in
                    _ = database.newFact(with: factEntity)
                }
                database.saveContext()
                if response.hasNext {
                    self?.updateFacts(currentVersion: currentVersion, offset: offset + response.facts.count, limit: limit, progressHandler: progress, completionHandler: completion)
                } else {
                    self?.reloadFacts()
                    completion()
                }
            case .failure(let error):
                print(error.localizedDescription)
            }
        }
    }
    
    func reloadFacts() {
        facts = database.fetchAll()
    }
    
    // MARK: - Getting facts
    
    func getFactsCount() -> Int {
        return facts.count
    }
    
    func getFact(at index: Int) -> Fact? {
        return (facts.count > index) ? facts[index] : nil
    }
    
}
