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
    
    var roomId: String!
    var timer: Timer?
    var questions: [GameRoomInfoQuestion]?
    
    var mySubviews = [UIView]()
    var oponentSubviews = [UIView]()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        kolodaView.dataSource = self
        kolodaView.delegate = self
        
        configureProgress()
        
        timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(refresh), userInfo: nil, repeats: true)
    }
    
    func configureProgress() {
        mySubviews = Array(0..<5).map({ (index) -> UIView in
            let label = UILabel()
            label.translatesAutoresizingMaskIntoConstraints = false
            label.textColor = .white
            label.text = String(index + 1)
            label.textAlignment = .center
            label.backgroundColor = .lightGray
            return label
        })
        let myStack = UIStackView(arrangedSubviews: mySubviews)
        myStack.spacing = 0.0
        myStack.axis = .horizontal
        myStack.alignment = .fill
        myStack.distribution = .fillEqually
        myStack.translatesAutoresizingMaskIntoConstraints = false
        
        oponentSubviews = Array(0..<5).map({ (index) -> UIView in
            let label = UILabel()
            label.translatesAutoresizingMaskIntoConstraints = false
            label.textColor = .white
            label.text = String(index + 1)
            label.textAlignment = .center
            label.backgroundColor = .lightGray
            return label
        })
        let oponentStack = UIStackView(arrangedSubviews: oponentSubviews)
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
    
    @objc func refresh() {
        networking.performRequest(to: EndpointCollection.onlineGameRoomInfo(id: roomId ?? "")) { [weak self] (result: Result<GameRoomInfoResponse, Error>) in
            switch result {
            case .success(let response):
                // Update question progress
                func colorForQuestion(num: Int, question: GameRoomInfoQuestion) -> UIColor {
                    switch num {
                    case 1:
                        return (question.useTrueQuestion) ? .green : .red
                    case 2:
                        return (!question.useTrueQuestion) ? .green : .red
                    default:
                        return .lightGray
                    }
                }
                DispatchQueue.main.async {
                    if self?.questions == nil {
                        self?.questions = response.questionsList
                        self?.kolodaView.reloadData()
                    }
                    
                    for (index, question) in response.answersPlayer1List.enumerated() {
                        let questionK = response.questionsList[index]
                        self?.mySubviews[index].backgroundColor = colorForQuestion(num: question, question: questionK)
                    }
                    for (index, question) in response.answersPlayer2List.enumerated() {
                        let questionK = response.questionsList[index]
                        self?.oponentSubviews[index].backgroundColor = colorForQuestion(num: question, question: questionK)
                    }
                    
                    if response.answersPlayer1List[4] != 0
                        && response.answersPlayer2List[4] != 0 {
                        
                        let myScore = response.answersPlayer1List.enumerated().map({ (index, elem) in
                            response.questionsList[index].useTrueQuestion && (elem == 1)
                        }).reduce(0, { (result, next) -> Int in
                            return result + (next ? 1 : 0)
                        })
                        
                        let win = (myScore
                            > response.answersPlayer2List.enumerated().map({ (index, elem) in
                                response.questionsList[index].useTrueQuestion && (elem == 1)
                            }).reduce(0, { (result, next) -> Int in
                                return result + (next ? 1 : 0)
                            }))
                        let alert = UIAlertController(title: win ? "You Win" : "You Loose", message: "Your score is \(myScore * 100)", preferredStyle: .alert)
                        alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: { (_) in
                            self?.navigationController?.popToRootViewController(animated: true)
                        }))
                        self?.present(alert, animated: true, completion: nil)
                    }
                }
            case .failure(let error):
                DispatchQueue.main.async {
                    self?.show(error: error)
                }
            }
        }
    }

    @IBAction func falseAction() {
        networking.performRequest(to: EndpointCollection.putAnswer, with: PutAnswerRequest(gameRoomId: roomId, answerNumber: jjj, answer: 2)) { [weak self] (error) in
            guard let error = error else { return }
            self?.show(error: error)
        }
        jjj += 1
        kolodaView.swipe(.left)
    }
    
    var jjj = 0
    @IBAction func trueAction() {
        networking.performRequest(to: EndpointCollection.putAnswer, with: PutAnswerRequest(gameRoomId: roomId, answerNumber: jjj, answer: 1)) { [weak self] (error) in
            guard let error = error else { return }
            self?.show(error: error)
        }
        jjj += 1
        kolodaView.swipe(.right)
    }
    
}

extension GameVC: KolodaViewDataSource, KolodaViewDelegate {
    
    func koloda(_ koloda: KolodaView, viewForCardAt index: Int) -> UIView {
//        guard let fact = FactsStore.shared.getFact(at: index) else {
//            preconditionFailure("Can't get fact")
//        }
        let factKek = questions![index]
        let fact = factKek.fact
        
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text = factKek.useTrueQuestion ? fact.trueFact : fact.falseFact
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
//        return FactsStore.shared.getFactsCount()
        return questions?.count ?? 0
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
