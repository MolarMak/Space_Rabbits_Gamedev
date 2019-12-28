//
//  SplashScreenVC.swift
//  iOS-Template
//
//  Created by Alex on 11.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import UIKit

class SplashScreenVC: UIViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NotificationCenter.default.addObserver(self, selector: #selector(route), name: authNotification.name, object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        segue()
    }
    
    func segue() {
        if User.current == nil {
            performSegue(withIdentifier: "auth", sender: self)
        } else {
            performSegue(withIdentifier: "menu", sender: self)
        }
    }
    
    @objc func route() {
        if let presented = presentedViewController {
            presented.dismiss(animated: true, completion: nil)
        }
    }
    
}
