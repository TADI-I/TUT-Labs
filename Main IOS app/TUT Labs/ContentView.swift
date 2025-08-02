//
//  ContentView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore on 8/2/25.
//

import SwiftUI

struct ContentView: View {
    @StateObject var authViewModel = AuthViewModel()
    @State private var showStudentView = false

    var body: some View {
        NavigationView {
            VStack {
                // Public access button
                Button("📘 View Lab Info Without Login") {
                    showStudentView = true
                }
                .padding()
                .sheet(isPresented: $showStudentView) {
                    StudentLabStatusView()
                }

                Divider()

                // Authenticated user routing
                if authViewModel.isLoggedIn {
                    if authViewModel.userRole == "admin" {
                        AdminDashboardView(authViewModel: authViewModel)
                    } else {
                        TutorDashboardView(authViewModel: authViewModel)
                    }
                } else {
                    LoginView(authViewModel: authViewModel)
                }
            }
            
        }
    }
}


