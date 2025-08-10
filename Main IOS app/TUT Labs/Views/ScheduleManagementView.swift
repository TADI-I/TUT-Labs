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
    @State private var selectedTime = "16:00 - 19:00"
    @State private var selectedLab = "Lab 10 - 138"
    @State private var showError = false
    @State private var showAddSuccess = false
    @State private var showDeleteConfirm = false
    @State private var shiftToDelete: LabShift?
    
    let days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    let times = ["16:00 - 19:00", "19:00 - 22:00", "18:00 - 20:00", "20:00 - 22:00"]
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
                    } else {
                        showAddSuccess = true
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
                                    shiftToDelete = shift
                                    showDeleteConfirm = true
                                } label: {
                                    Image(systemName: "trash")
                                }
                                .buttonStyle(BorderlessButtonStyle())
                            }
                        }
                    }
                }
            }
            
  
        }
        Button("Download Weekly Lab Report") {
            let calendar = Calendar.current
            let today = Date()
            guard
                let startOfWeek = calendar.date(from: calendar.dateComponents([.yearForWeekOfYear, .weekOfYear], from: today)),
                let endOfWeek = calendar.date(byAdding: .day, value: 6, to: startOfWeek)
            else {
                print("Error calculating week range")
                return
            }

            viewModel.fetchWeeklyLabSessions(startDate: startOfWeek, endDate: endOfWeek) { sessions in
                guard let pdfURL = viewModel.generateWeeklyLabReport(
                    sessions: sessions,
                    weekRange: "\(startOfWeek.formatted(date: .abbreviated, time: .omitted)) - \(endOfWeek.formatted(date: .abbreviated, time: .omitted))"
                ) else {
                    print("Failed to generate PDF")
                    return
                }

                DispatchQueue.main.async {
                    if let rootVC = UIApplication.shared.connectedScenes
                        .compactMap({ $0 as? UIWindowScene })
                        .flatMap({ $0.windows })
                        .first(where: { $0.isKeyWindow })?
                        .rootViewController {
                        
                        let activityVC = UIActivityViewController(activityItems: [pdfURL], applicationActivities: nil)
                        rootVC.present(activityVC, animated: true)
                    } else {
                        print("Unable to find root view controller")
                    }
                }
            }
        }
        .buttonStyle(.borderedProminent)
        .navigationTitle("Schedule Management")
        // Alert for duplicate error
        .alert("Shift Already Exists", isPresented: $showError) {
            Button("OK", role: .cancel) { showError = false }
        } message: {
            Text("This shift has already been assigned. Please select a different day, time, or lab.")
        }
        
        // Alert for successful addition
        .alert("Shift Assigned", isPresented: $showAddSuccess) {
            Button("OK", role: .cancel) { showAddSuccess = false }
        } message: {
            Text("The shift has been successfully assigned.")
        }
        
        // Confirmation alert before deleting
        .alert("Delete Shift?", isPresented: $showDeleteConfirm) {
            Button("Delete", role: .destructive) {
                if let shift = shiftToDelete {
                    viewModel.deleteShift(shift: shift)
                }
                shiftToDelete = nil
            }
            Button("Cancel", role: .cancel) {
                shiftToDelete = nil
            }
        } message: {
            Text("Are you sure you want to delete this shift?")
        }
    }
}
