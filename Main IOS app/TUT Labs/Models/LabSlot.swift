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

