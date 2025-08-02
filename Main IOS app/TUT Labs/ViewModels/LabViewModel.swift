//
//  LabViewModel.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//
import Foundation
import SwiftUI
import FirebaseFirestore
import FirebaseAuth

class LabViewModel: ObservableObject {
    @Published var tutors: [User] = []
    @Published var labShifts: [LabShift] = []
    @Published var labStatuses: [LabStatus] = []
    @Published var shiftsForCurrentTutor: [LabShift] = []
    
    private var db = Firestore.firestore()
    
    init() {
        fetchTutors()
        fetchShifts()
        fetchLabStatuses()
    }
    
    func fetchLabStatuses() {
        db.collection("labStatus").order(by: "timestamp", descending: true).getDocuments { snapshot, error in
            if let error = error {
                print("Error fetching lab statuses: \(error.localizedDescription)")
                return
            }

            guard let documents = snapshot?.documents else {
                print("No lab statuses found")
                return
            }

            let statuses = documents.compactMap { doc -> LabStatus? in
                let data = doc.data()
                guard let labName = data["labName"] as? String,
                      let isOpen = data["labOpen"] as? Bool,
                      let note = data["note"] as? String,
                      let updatedBy = data["updatedBy"] as? String,
                      let updatedById = data["updatedById"] as? String,
                      let timestamp = data["timestamp"] as? Timestamp else {
                    return nil
                }

                return LabStatus(
                    id: UUID(uuidString: doc.documentID) ?? UUID(),
                    labName: labName,
                    isOpen: isOpen,
                    note: note,
                    updatedBy: updatedBy,
                    updatedById: updatedById,
                    timestamp: timestamp.dateValue()
                )
            }

            DispatchQueue.main.async {
                print("Successfully loaded \(statuses.count) lab statuses")
                self.labStatuses = statuses
            }
        }
    }


    
    // Fetch all tutors with role "tutor"
    func fetchTutors() {
        db.collection("users")
            .whereField("role", isEqualTo: "tutor")
            .getDocuments { snapshot, error in
                if let error = error {
                    print("Error fetching tutors: \(error.localizedDescription)")
                    return
                }
                guard let documents = snapshot?.documents else {
                    print("No tutors found")
                    return
                }
                let fetchedTutors = documents.compactMap { doc -> User? in
                    let data = doc.data()
                    guard
                        let name = data["name"] as? String,
                        let email = data["email"] as? String,
                        let role = data["role"] as? String
                    else {
                        return nil
                    }
                    return User(id: doc.documentID, name: name, email: email, role: role)
                }
                DispatchQueue.main.async {
                    self.tutors = fetchedTutors
                }
            }
    }
    
    // Add a new tutor to Firestore and refresh list
    func addTutor(name: String, email: String) {
        // Generate a default password or set a temporary one
        let tempPassword = "Welcome123!"  // You can generate a stronger one too
        
        Auth.auth().createUser(withEmail: email, password: tempPassword) { result, error in
            if let error = error {
                print("Failed to create auth account: \(error.localizedDescription)")
                return
            }
            
            guard let uid = result?.user.uid else { return }
            
            let tutorData: [String: Any] = [
                "name": name,
                "email": email,
                "role": "tutor"
            ]
            
            self.db.collection("users").document(uid).setData(tutorData) { error in
                if let error = error {
                    print("Failed to save tutor to Firestore: \(error.localizedDescription)")
                } else {
                    print("Tutor added successfully")
                    
                    // Refresh the local list
                    self.fetchTutors()
                }
            }
        }
    }
    
    // Remove a tutor by their document ID and refresh list
    func removeTutor(tutorId: String) {
        db.collection("users").document(tutorId).delete() { error in
            if let error = error {
                print("Error removing tutor: \(error.localizedDescription)")
            } else {
                print("Tutor removed successfully")
                self.fetchTutors() // Refresh list after deleting
            }
        }
    }
    
    // Assign a lab shift to a tutor (save locally + Firestore)
    func assignShift(tutorId: String, day: String, time: String, labName: String) -> Bool {
        let duplicateExists = labShifts.contains { shift in
            shift.labName == labName &&
            shift.day == day &&
            shift.time == time
        }
        
        guard !duplicateExists else {
            return false
        }
        
        let newShift = LabShift(tutorId: tutorId, day: day, time: time, labName: labName)
        labShifts.append(newShift)
        saveShiftToFirestore(shift: newShift)
        return true
    }


    func deleteShift(shift: LabShift) {
        // Remove locally
        if let index = labShifts.firstIndex(where: { $0.id == shift.id }) {
            labShifts.remove(at: index)
        }
        
        // Remove from Firestore
        db.collection("labShifts").document(shift.id.uuidString).delete { error in
            if let error = error {
                print("Error deleting shift: \(error.localizedDescription)")
            } else {
                print("Shift deleted successfully!")
            }
        }
    }

    
    // Save shift document to Firestore with ID = UUID string
    private func saveShiftToFirestore(shift: LabShift) {
        let data: [String: Any] = [
            "tutorId": shift.tutorId,
            "day": shift.day,
            "time": shift.time,
            "labName": shift.labName
        ]
        
        db.collection("labShifts").document(shift.id.uuidString).setData(data) { error in
            if let error = error {
                print("Error writing shift to Firestore: \(error.localizedDescription)")
            } else {
                print("Shift saved successfully!")
            }
        }
    }
    
    // Fetch all lab shifts
    func fetchShifts() {
        db.collection("labShifts").getDocuments { snapshot, error in
            if let error = error {
                print("Error fetching shifts: \(error.localizedDescription)")
                return
            }
            
            guard let documents = snapshot?.documents else {
                print("No shifts found")
                return
            }
            
            let fetchedShifts = documents.compactMap { doc -> LabShift? in
                let data = doc.data()
                guard
                    let tutorId = data["tutorId"] as? String,
                    let day = data["day"] as? String,
                    let time = data["time"] as? String,
                    let labName = data["labName"] as? String
                else {
                    return nil
                }
                return LabShift(id: UUID(uuidString: doc.documentID) ?? UUID(), tutorId: tutorId, day: day, time: time, labName: labName)
            }
            
            DispatchQueue.main.async {
                self.labShifts = fetchedShifts
            }
        }
    }
    
    // Get shifts filtered for a specific tutor
    func shiftsForTutor(_ tutorId: String) -> [LabShift] {
        labShifts.filter { $0.tutorId == tutorId }
    }
    
    func fetchShiftsForCurrentTutor() {
            guard let uid = Auth.auth().currentUser?.uid else {
                print("No logged-in user")
                self.shiftsForCurrentTutor = []
                return
            }
            print("Fetching shifts for tutorId:", uid)

            db.collection("labShifts")
                .whereField("tutorId", isEqualTo: uid)
                .getDocuments { snapshot, error in
                    if let error = error {
                        print("Error fetching shifts: \(error.localizedDescription)")
                        return
                    }

                    guard let documents = snapshot?.documents else {
                        print("No shifts found")
                        self.shiftsForCurrentTutor = []
                        return
                    }

                    print("Firestore returned \(documents.count) documents")

                    let shifts = documents.compactMap { doc -> LabShift? in
                        let data = doc.data()
                        guard let day = data["day"] as? String,
                              let time = data["time"] as? String,
                              let labName = data["labName"] as? String else {
                            return nil
                        }

                        return LabShift(id: UUID(uuidString: doc.documentID) ?? UUID(),
                                        tutorId: uid,
                                        day: day,
                                        time: time,
                                        labName: labName)
                    }

                    DispatchQueue.main.async {
                        self.shiftsForCurrentTutor = shifts
                    }
                }
        }

}

