//
//  AuthViewModel.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import Foundation
import FirebaseAuth
import FirebaseFirestore
import SwiftUI

class AuthViewModel: ObservableObject {
    @Published var isLoggedIn = false
    @Published var userRole: String? = nil
    @Published var errorMessage = ""
    
    init() {
        Auth.auth().addStateDidChangeListener { _, user in
            if let user = user {
                self.fetchUserRole(uid: user.uid)
            } else {
                DispatchQueue.main.async {
                    self.isLoggedIn = false
                    self.userRole = nil
                }
            }
        }
    }

    func login(email: String, password: String) {
        Auth.auth().signIn(withEmail: email, password: password) { result, error in
            if let error = error {
                DispatchQueue.main.async {
                    self.errorMessage = error.localizedDescription
                }
                return
            }
            
            guard let uid = result?.user.uid else {
                DispatchQueue.main.async {
                    self.errorMessage = "User ID not found."
                }
                return
            }
            
            self.fetchUserRole(uid: uid)
        }
    }

    private func fetchUserRole(uid: String) {
        let db = Firestore.firestore()
        db.collection("users").document(uid).getDocument { document, error in
            DispatchQueue.main.async {
                if let document = document, document.exists {
                    let data = document.data()
                    self.userRole = data?["role"] as? String
                    self.isLoggedIn = true
                } else {
                    self.errorMessage = "Could not fetch user role."
                    self.isLoggedIn = false
                    self.userRole = nil
                }
            }
        }
    }

    func logout() {
        do {
            try Auth.auth().signOut()
            DispatchQueue.main.async {
                self.isLoggedIn = false
                self.userRole = nil
            }
        } catch {
            DispatchQueue.main.async {
                self.errorMessage = error.localizedDescription
            }
        }
    }
}
