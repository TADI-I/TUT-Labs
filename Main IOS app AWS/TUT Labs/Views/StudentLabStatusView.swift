//
//  StudentLabStatusView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//
import SwiftUI

struct StudentLabStatusView: View {
    @StateObject private var labViewModel = LabViewModel()
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("🟢 Current Lab Statuses")) {
                    ForEach(labViewModel.labStatuses) { status in
                        VStack(alignment: .leading) {
                            HStack {
                                Text(status.labName)
                                    .font(.headline)
                                Spacer()
                                Text(status.isOpen ? "Open" : "Closed")
                                    .foregroundColor(status.isOpen ? .green : .red)
                            }
                            if !status.note.isEmpty {
                                Text("Note: \(status.note)")
                                    .font(.subheadline)
                                    .foregroundColor(.gray)
                            }
                            Text("Updated: \(status.timestamp.formatted(date: .abbreviated, time: .shortened))")
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
                        .padding(.vertical, 4)
                    }
                }

                
                Section(header: Text("📅 Lab Roster")) {
                    ForEach(labViewModel.labShifts) { shift in
                        VStack(alignment: .leading) {
                            Text("Lab: \(shift.labName)")
                                .font(.headline)
                            Text("Time: \(shift.day), \(shift.time)")
                                .font(.subheadline)
                            if let tutor = labViewModel.tutors.first(where: { $0.id == shift.tutorId }) {
                                Text("Tutor: \(tutor.name)")
                                    .font(.subheadline)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
                
                
            }
            .navigationTitle(" Lab Info")
            .refreshable {
                labViewModel.fetchShifts()
                labViewModel.fetchTutors()
                labViewModel.fetchLabStatuses()
            }
            .onAppear {
                labViewModel.fetchShifts()
                labViewModel.fetchTutors()
                labViewModel.fetchLabStatuses()
                
            }
            
        }
        Text("Made by Tadiwanashe Songore For TUT FoICT")
            .font(.footnote)
            .foregroundColor(.gray)
            .padding(.top, 30)
            .padding(.bottom, 10)
    }
}



