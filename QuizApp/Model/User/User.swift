//
//  User.swift
//  iOS-Template
//
//  Created by Alex on 11.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import Foundation
import SwiftKeychainWrapper

struct User {
    
    private static let kLOGIN = "login"
    private static let kPASSWORD = "password"
    private static let kTOKEN = "token"
        
    static private var _current: UserModel?
    static var current: UserModel? {
        guard let cachedUser = _current else {
            if let login = KeychainWrapper.standard.string(forKey: kLOGIN),
                let password = KeychainWrapper.standard.string(forKey: kPASSWORD),
                let token = KeychainWrapper.standard.string(forKey: kTOKEN) {
                _current = UserModel(login: login, password: password, token: token)
            }
            return _current
        }
        return cachedUser
    }
    
    // MARK: - Update
    
    @discardableResult
    static func update(login: String) -> Bool {
        defer {
            _current = nil
        }
        return KeychainWrapper.standard.set(login, forKey: kLOGIN)
    }
    
    @discardableResult
    static func update(password: String) -> Bool {
        defer {
            _current = nil
        }
        return KeychainWrapper.standard.set(password, forKey: kPASSWORD)
    }
    
    @discardableResult
    static func update(token: String) -> Bool {
        defer {
            _current = nil
        }
        return KeychainWrapper.standard.set(token, forKey: kTOKEN)
    }
    
    // MARK: - Save / Clear
    
    @discardableResult
    static func save(model: UserModel) -> Bool {
        var saveSuccessful = true
        saveSuccessful = saveSuccessful && KeychainWrapper.standard.set(model.login, forKey: kLOGIN)
        saveSuccessful = saveSuccessful && KeychainWrapper.standard.set(model.password, forKey: kPASSWORD)
        saveSuccessful = saveSuccessful && KeychainWrapper.standard.set(model.token, forKey: kTOKEN)
        if saveSuccessful {
            _current = model
        }
        return saveSuccessful
    }
    
    @discardableResult
    static func clear() -> Bool {
        var clearSuccessful = true
        clearSuccessful = clearSuccessful && KeychainWrapper.standard.removeObject(forKey: kLOGIN)
        clearSuccessful = clearSuccessful && KeychainWrapper.standard.removeObject(forKey: kPASSWORD)
        clearSuccessful = clearSuccessful && KeychainWrapper.standard.removeObject(forKey: kTOKEN)
        return clearSuccessful
    }
    
}

struct UserModel {
    let login: String
    let password: String
    let token: String
}
