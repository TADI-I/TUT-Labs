//
//  LabSlot.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import Foundation

struct LabStatus: Identifiable {
    var id = UUID()
    var labName: String
    var isOpen: Bool
    var note: String
    var updatedBy: String
    var updatedById: String
    var timestamp: Date
}

struct LabShift: Identifiable {
    var id = UUID()
    var tutorId: String
    var day: String
    var time: String
    var labName: String
}


struct LabSession: Identifiable {
    let id: String
    let labName: String
    let openedById: String
    let openedByName: String
    let openedAt: Date
    let closedById: String?
    let closedByName: String?
    let closedAt: Date?
    let note: String
}

