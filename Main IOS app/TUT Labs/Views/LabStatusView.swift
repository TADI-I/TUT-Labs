//
//  LabStatusView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import SwiftUI
import FirebaseFirestore
import FirebaseAuth

struct LabStatusView: View {
    let isAdmin: Bool

    @State private var selectedLab = "Lab 10 - 138"
    @State private var labName = "10-138"
    @State private var labOpen = false
    @State private var note = ""
    @State private var showConfirmation = false
    @State private var currentUserId = Auth.auth().currentUser?.uid
    @State private var labStatuses: [String: LabStatus] = [:]

    let labs = ["Lab 10 - 138", "Lab 10 - G10", "Lab 10 - G06"]

    var body: some View {
        Form {
            Section(header: Text("Select Lab")) {
                Picker("Lab", selection: $selectedLab) {
                    ForEach(labs, id: \.self) { lab in
                        Text(lab)
                    }
                }
                .onChange(of: selectedLab) { _ in
                    fetchStatus(for: selectedLab)
                }
            }

            Section(header: Text("Lab Status")) {
                Toggle("Lab is Open", isOn: $labOpen)
                    .disabled(!canEditStatus())
                
                TextField("Optional note", text: $note)
                    .disabled(!canEditStatus())
            }

            if canEditStatus() {
                Section {
                    Button("Update Status") {
                        updateStatus()
                    }
                    .buttonStyle(.borderedProminent)
                }
            }
        }
        .navigationTitle(isAdmin ? "Admin Announcements" : "Update Lab Status")
        .onAppear {
            fetchStatus(for: selectedLab)
        }
        .alert("Status Updated!", isPresented: $showConfirmation) {
            Button("OK", role: .cancel) { }
        }
    }

    func canEditStatus() -> Bool {
        guard let status = labStatuses[selectedLab] else {
            return true // No status yet, allow creation
        }

        // If the lab is OPEN, only the person who opened it or admin can close it
        if status.isOpen {
            return status.updatedById == currentUserId || isAdmin
        }

        // If the lab is CLOSED, anyone (including different users) can open it
        return true
    }

    func fetchStatus(for lab: String) {
        let db = Firestore.firestore()
        db.collection("labStatus").document(lab).getDocument { doc, error in
            guard let data = doc?.data(), error == nil else {
                self.labOpen = false
                self.note = ""
                self.labStatuses[lab] = nil
                return
            }

            let status = LabStatus(
                id: UUID(uuidString: doc?.documentID ?? "") ?? UUID(),
                labName: lab,
                isOpen: data["labOpen"] as? Bool ?? false,
                note: data["note"] as? String ?? "",
                updatedBy: data["updatedBy"] as? String ?? "Unknown",
                updatedById: data["updatedById"] as? String ?? "",
                timestamp: (data["timestamp"] as? Timestamp)?.dateValue() ?? Date()
                
            )

            self.labStatuses[lab] = status
            self.labOpen = status.isOpen
            self.note = status.note
        }
    }

    func updateStatus() {
        guard let uid = currentUserId else { return }

        let db = Firestore.firestore()
        let data: [String: Any] = [
            "labName": selectedLab,
            "labOpen": labOpen,
            "note": note,
            "updatedBy": isAdmin ? "Admin" : "Tutor",
            "updatedById": uid,
            "timestamp": FieldValue.serverTimestamp()
        ]

        db.collection("labStatus").document(selectedLab).setData(data) { error in
            if let error = error {
                print("Error updating lab status: \(error.localizedDescription)")
            } else {
                print("Lab status updated for \(selectedLab)")
                showConfirmation = true
                fetchStatus(for: selectedLab)
            }
        }
    }
    
}
