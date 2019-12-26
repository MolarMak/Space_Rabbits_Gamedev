//
//  GameVC.swift
//  QuizApp
//
//  Created by Alex on 21.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import UIKit
import Koloda

class GameVC: UIViewController {

    @IBOutlet weak var kolodaView: KolodaView!
    @IBOutlet weak var oponentPhotoView: UIImageView!
    @IBOutlet weak var myPhotoView: UIImageView!
    @IBOutlet weak var oponentProgressContainer: UIView!
    @IBOutlet weak var myProgressContainer: UIView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        kolodaView.dataSource = self
        kolodaView.delegate = self
        
        configureProgress()
    }
    
    func configureProgress() {
        let myStack = UIStackView(arrangedSubviews: Array(0..<15).map({ (index) -> UIView in
            let label = UILabel()
            label.translatesAutoresizingMaskIntoConstraints = false
            label.textColor = .white
            label.text = String(index + 1)
            label.textAlignment = .center
            label.backgroundColor = .blue
            return label
        }))
        myStack.spacing = 0.0
        myStack.axis = .horizontal
        myStack.alignment = .fill
        myStack.distribution = .fillEqually
        myStack.translatesAutoresizingMaskIntoConstraints = false
        let oponentStack = UIStackView(arrangedSubviews: Array(0..<15).map({ (index) -> UIView in
            let label = UILabel()
            label.translatesAutoresizingMaskIntoConstraints = false
            label.textColor = .white
            label.text = String(index + 1)
            label.textAlignment = .center
            label.backgroundColor = .blue
            return label
        }))
        oponentStack.spacing = 0.0
        oponentStack.axis = .horizontal
        oponentStack.alignment = .fill
        oponentStack.distribution = .fillEqually
        oponentStack.translatesAutoresizingMaskIntoConstraints = false
        
        myProgressContainer.addSubview(myStack)
        oponentProgressContainer.addSubview(oponentStack)
        
        myProgressContainer.leftAnchor.constraint(equalTo: myStack.leftAnchor).isActive = true
        myProgressContainer.topAnchor.constraint(equalTo: myStack.topAnchor).isActive = true
        myProgressContainer.rightAnchor.constraint(equalTo: myStack.rightAnchor).isActive = true
        myProgressContainer.bottomAnchor.constraint(equalTo: myStack.bottomAnchor).isActive = true
        
        oponentProgressContainer.leftAnchor.constraint(equalTo: oponentStack.leftAnchor).isActive = true
        oponentProgressContainer.topAnchor.constraint(equalTo: oponentStack.topAnchor).isActive = true
        oponentProgressContainer.rightAnchor.constraint(equalTo: oponentStack.rightAnchor).isActive = true
        oponentProgressContainer.bottomAnchor.constraint(equalTo: oponentStack.bottomAnchor).isActive = true
    }

    @IBAction func falseAction() {
        kolodaView.swipe(.left)
    }
    
    @IBAction func trueAction() {
        kolodaView.swipe(.right)
    }
    
}

extension GameVC: KolodaViewDataSource, KolodaViewDelegate {
    
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
