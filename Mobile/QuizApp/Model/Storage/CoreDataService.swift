//
//  CoreDataService.swift
//  iOS-Template
//
//  Created by Alex on 11.12.2019.
//  Copyright © 2019 Alex. All rights reserved.
//

import Foundation
import CoreData

class CoreDataService {
    
    init() {
        _ = persistentContainer
    }
    
    // MARK: - Core Data stack

    lazy var persistentContainer: NSPersistentContainer = {
        let container = NSPersistentContainer(name: "CoreData")
        container.loadPersistentStores(completionHandler: { (_, error) in
            if let error = error as NSError? {
                fatalError("Unresolved error \(error), \(error.userInfo)")
            }
        })
        container.viewContext.mergePolicy = NSMergePolicy(merge: .overwriteMergePolicyType)
        return container
    }()
    
    // MARK: - Core Data Saving support

    func saveContext () {
        let context = persistentContainer.viewContext
        if context.hasChanges {
            do {
                try context.save()
            } catch {
                let nserror = error as NSError
                fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
            }
        }
    }
    
    // MARK: - Fetch & Delete
    
    func fetchOne<T: NSManagedObject>(predicate: NSPredicate? = nil) -> T? {
        guard let fetchRequest: NSFetchRequest<T> = T.fetchRequest() as? NSFetchRequest<T> else {
            preconditionFailure("Can't get fetchRequest")
        }
        fetchRequest.fetchLimit = 1
        fetchRequest.predicate = predicate
        do {
            let models = try persistentContainer.viewContext.fetch(fetchRequest)
            return models.first
        } catch {
            preconditionFailure("Can't fetch models")
        }
    }
    
    func fetchAll<T: NSManagedObject>(predicate: NSPredicate? = nil) -> [T] {
        guard let fetchRequest: NSFetchRequest<T> = T.fetchRequest() as? NSFetchRequest<T> else {
            preconditionFailure("Can't get fetchRequest")
        }
        fetchRequest.predicate = predicate
        do {
            let models = try persistentContainer.viewContext.fetch(fetchRequest)
            return models
        } catch {
            preconditionFailure("Can't fetch models")
        }
    }
    
    func delete<T: NSManagedObject>(model: T) {
        persistentContainer.viewContext.delete(model)
    }
    
}

extension CoreDataService {
    
    func newFact(with data: FactEntityResponse) -> Fact {
        defer {
            print("Saved fact: \(data)")
        }
        let fact = Fact(context: persistentContainer.viewContext)
        fact.id = Int64(data.id)
        fact.fact = data.fact
        fact.factVersion = Int16(data.factVersion)
        fact.falseFact = data.falseFact
        fact.trueFact = data.trueFact
        return fact
    }
    
}
