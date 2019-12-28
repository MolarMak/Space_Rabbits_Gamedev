//
//  UserDefaultsService.swift
//  iOS-Template
//
//  Created by Alex on 11.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import Foundation

class UserDefaultsService {

    var metrics: Int? {
        get {
            return UserDefaults.standard.integer(forKey: "metrics")
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "metrics")
            UserDefaults.standard.synchronize()
        }
    }

    func clear() {
        metrics = nil
    }

}
