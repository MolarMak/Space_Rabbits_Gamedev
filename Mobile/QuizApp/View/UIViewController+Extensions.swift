//
//  UIViewController+Extensions.swift
//  iOS-Template
//
//  Created by Alex on 12.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import UIKit

extension UIViewController {
    
    func show(info: String) {
        let alert = UIAlertController(title: info, message: nil, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
        present(alert, animated: true, completion: nil)
    }
    
    func show(error: Error) {
        let alert = UIAlertController(title: "Error", message: error.localizedDescription, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
        present(alert, animated: true, completion: nil)
    }
    
}
