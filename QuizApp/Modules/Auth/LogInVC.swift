//
//  LogInVC.swift
//  iOS-Template
//
//  Created by Alex on 11.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import UIKit

class LogInVC: UIViewController {

    @IBOutlet weak var loginTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    
    @IBAction func logIn(_ sender: UIButton) {
        let login = loginTextField.text ?? ""
        let password = passwordTextField.text ?? ""
        let request = LoginRequest(login: login, password: password)
        networking.performRequest(to: EndpointCollection.login, with: request) { [weak self] (result: Result<LoginResponse, Error>) in
            switch result {
            case .success(let response):
                User.save(model: UserModel(login: login, password: password, token: response.token))
                DispatchQueue.main.async {
                    NotificationCenter.default.post(authNotification)
                }
            case .failure(let error):
                DispatchQueue.main.async {
                    self?.show(error: error)
                }
            }
        }
    }
    
}
