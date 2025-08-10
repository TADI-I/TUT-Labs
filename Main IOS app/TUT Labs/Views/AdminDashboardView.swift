//
//  AdminDashboardView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import SwiftUI

struct AdminDashboardView: View {
    
    @ObservedObject var authViewModel: AuthViewModel
    var body: some View {
        NavigationView {
            List {
                NavigationLink("Manage Tutors", destination: ManageTutorsView(viewModel: LabViewModel()))
                //NavigationLink("Assign Schedule", destination: ScheduleView())
                NavigationLink("Manage Schedule", destination: ScheduleManagementView(viewModel: LabViewModel()))
                NavigationLink("Open/Close Labs", destination: LabStatusView(isAdmin: true))
            }
            .navigationTitle("Admin Dashboard")
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
