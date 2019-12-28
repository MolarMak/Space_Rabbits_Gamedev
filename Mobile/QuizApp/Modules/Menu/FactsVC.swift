//
//  FactsVC.swift
//  iOS-Template
//
//  Created by Alex on 12.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import UIKit
import Koloda

class FactsVC: UIViewController {

    @IBOutlet weak var kolodaView: KolodaView!
    
//    @IBOutlet weak var falseView: UIView!
//    @IBOutlet weak var trueView: UIView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        kolodaView.dataSource = self
        kolodaView.delegate = self
        
//        falseView.layer.cornerRadius = 8.0
//        falseView.layer.borderWidth = 8.0
//        falseView.layer.borderColor = UIColor.systemRed.cgColor
//        falseView.alpha = 0.0
        
//        trueView.layer.cornerRadius = 8.0
//        trueView.layer.borderWidth = 8.0
//        trueView.layer.borderColor = UIColor.systemGreen.cgColor
//        trueView.alpha = 0.0
        
        FactsStore.shared.runFactsUpdate(progressHandler: { (progress) in
            print(progress)
        }, completionHandler: { [weak self] in
            DispatchQueue.main.async {
                print("Completed update")
                FactsStore.shared.reloadFacts()
                self?.kolodaView.reloadData()
            }
        })
    }
    
    @IBAction func back() {
        kolodaView.revertAction()
    }

}

extension FactsVC: KolodaViewDataSource, KolodaViewDelegate {
    
    func koloda(_ koloda: KolodaView, viewForCardAt index: Int) -> UIView {
        guard let fact = FactsStore.shared.getFact(at: index) else {
            preconditionFailure("Can't get fact")
        }
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text = fact.fact
        label.textAlignment = .center
        label.font = UIFont.boldSystemFont(ofSize: 32.0)
        let image = UIImageView()
        image.translatesAutoresizingMaskIntoConstraints = false
        image.image = UIImage(named: "mock")
        let stack = UIStackView(arrangedSubviews: [image, label])
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .vertical
        stack.alignment = .fill
        stack.distribution = .fillEqually
        let containerView = UIView()
        containerView.translatesAutoresizingMaskIntoConstraints = false
        containerView.addSubview(stack)
        containerView.leftAnchor.constraint(equalTo: stack.leftAnchor).isActive = true
        containerView.topAnchor.constraint(equalTo: stack.topAnchor).isActive = true
        containerView.rightAnchor.constraint(equalTo: stack.rightAnchor).isActive = true
        containerView.bottomAnchor.constraint(equalTo: stack.bottomAnchor).isActive = true
        containerView.layer.borderColor = UIColor(red: 0.88, green: 0.83, blue: 0.81, alpha: 1.0).cgColor
        containerView.layer.borderWidth = 1.0
        containerView.layer.masksToBounds = true
        containerView.layer.cornerRadius = 6.0
        containerView.backgroundColor = UIColor(red: 1.00, green: 0.98, blue: 0.95, alpha: 1.0)
        return containerView
    }
    
    func kolodaNumberOfCards(_ koloda: KolodaView) -> Int {
        return FactsStore.shared.getFactsCount()
    }
    
    func koloda(_ koloda: KolodaView, draggedCardWithPercentage finishPercentage: CGFloat, in direction: SwipeResultDirection) {
//        switch direction {
//        case .left, .topLeft, .bottomLeft:
//            falseView.alpha = finishPercentage / 100.0
//        case .right, .topRight, .bottomRight:
//            trueView.alpha = finishPercentage / 100.0
//        default:
//            break
//        }
    }
    
    func koloda(_ koloda: KolodaView, didSwipeCardAt index: Int, in direction: SwipeResultDirection) {
//        falseView.alpha = 0.0
//        trueView.alpha = 0.0
    }
    
    func kolodaDidResetCard(_ koloda: KolodaView) {
//        falseView.alpha = 0.0
//        trueView.alpha = 0.0
    }
    
}
