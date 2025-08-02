//
//  ScheduleManagementView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import SwiftUI

struct ScheduleManagementView: View {
    @ObservedObject var viewModel: LabViewModel
    
    @State private var selectedTutorId: String? = nil
    @State private var selectedDay = "Monday"
    @State private var selectedTime = "09:00 - 11:00"
    @State private var selectedLab = "Lab 10 - 138"
    @State private var showError = false
    
    let days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    let times = ["09:00 - 11:00", "11:00 - 13:00", "14:00 - 16:00"]
    let labs = ["Lab 10 - 138", "Lab 10 - G10", "Lab 10 - G06"]
    
    var body: some View {
        Form {
            Section(header: Text("Select Tutor")) {
                Picker("Tutor", selection: $selectedTutorId) {
                    Text("Select a tutor").tag(String?.none)
                    ForEach(viewModel.tutors) { tutor in
                        Text(tutor.name).tag(Optional(tutor.id))
                    }
                }
            }
            
            Section(header: Text("Select Shift Details")) {
                Picker("Day", selection: $selectedDay) {
                    ForEach(days, id: \.self) { day in
                        Text(day)
                    }
                }
                
                Picker("Time", selection: $selectedTime) {
                    ForEach(times, id: \.self) { time in
                        Text(time)
                    }
                }
                
                Picker("Lab", selection: $selectedLab) {
                    ForEach(labs, id: \.self) { lab in
                        Text(lab)
                    }
                }
            }
            
            Section {
                Button("Assign Shift") {
                    guard let tutorId = selectedTutorId else { return }
                    if !viewModel.assignShift(tutorId: tutorId, day: selectedDay, time: selectedTime, labName: selectedLab) {
                        showError = true
                    }
                }
                .disabled(selectedTutorId == nil)
            }
            
            Section(header: Text("Assigned Shifts")) {
                List {
                    ForEach(viewModel.labShifts) { shift in
                        if let tutor = viewModel.tutors.first(where: { $0.id == shift.tutorId }) {
                            HStack {
                                VStack(alignment: .leading) {
                                    Text(tutor.name)
                                        .font(.headline)
                                    Text("\(shift.day), \(shift.time) at \(shift.labName)")
                                        .font(.subheadline)
                                }
                                Spacer()
                                Button(role: .destructive) {
                                    viewModel.deleteShift(shift: shift)
                                } label: {
                                    Image(systemName: "trash")
                                }
                                .buttonStyle(BorderlessButtonStyle()) // Allows button inside List row
                            }
                        }
                    }
                }
            }

        }
        .navigationTitle("Schedule Management")
        .alert("Error", isPresented: $showError, actions: {
            Button("OK", role: .cancel) { }
        }, message: {
            Text("This lab is already assigned for the selected day and time.")
        })
    }
}
