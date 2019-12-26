//
//  SearchVC.swift
//  QuizApp
//
//  Created by Alex on 21.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import UIKit

class SearchVC: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(3)) { [weak self] in
            self?.performSegue(withIdentifier: "game", sender: nil)
        }
    }

}
