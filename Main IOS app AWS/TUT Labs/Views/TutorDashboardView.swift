//
//  TutorDashboardView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import SwiftUI

struct TutorDashboardView: View {
    @ObservedObject var authViewModel: AuthViewModel
    var body: some View {
        NavigationView {
            List {
                NavigationLink("My Schedule", destination: ScheduleView())
                NavigationLink("Update Lab Status", destination: LabStatusView(isAdmin: false))
            }
            .navigationTitle("Tutor Dashboard")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Logout") {
                        authViewModel.logout()
                    }
                    .foregroundColor(.red)
                }
            }
        }
    }
}
