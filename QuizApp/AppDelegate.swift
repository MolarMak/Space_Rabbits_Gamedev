//
//  AppDelegate.swift
//  iOS-Template
//
//  Created by Alex on 11.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import UIKit
import CoreData
import IQKeyboardManager

let defaults = UserDefaultsService()
let database = CoreDataService()
let networking = NetworkingService()

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        IQKeyboardManager.shared().isEnabled = true

        return true
    }

    func applicationWillTerminate(_ application: UIApplication) {
        database.saveContext()
    }

}
