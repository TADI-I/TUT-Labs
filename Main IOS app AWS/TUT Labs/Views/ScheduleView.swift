//
//  ScheduleView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import SwiftUI
import FirebaseAuth

struct ScheduleView: View {
    @StateObject var viewModel = LabViewModel()  // Assume contains fetchTutorWeeklyShifts and generateTutorWeeklySchedulePDF

    var body: some View {
        VStack {
            List {
                if viewModel.shiftsForCurrentTutor.isEmpty {
                    Text("No shifts assigned.")
                        .foregroundColor(.gray)
                } else {
                    ForEach(viewModel.shiftsForCurrentTutor) { shift in
                        VStack(alignment: .leading) {
                            Text("\(shift.day) - \(shift.labName)")
                                .font(.headline)
                            Text(shift.time)
                                .font(.subheadline)
                                .foregroundColor(.gray)
                        }
                    }
                }
            }

            Button("Download Weekly Schedule") {
                guard let tutorId = Auth.auth().currentUser?.uid else {
                    print("No logged in user")
                    return
                }
                let calendar = Calendar.current
                let today = Date()
                guard
                    let startOfWeek = calendar.date(from: calendar.dateComponents([.yearForWeekOfYear, .weekOfYear], from: today)),
                    let endOfWeek = calendar.date(byAdding: .day, value: 6, to: startOfWeek)
                else {
                    print("Error calculating week range")
                    return
                }

                viewModel.fetchTutorWeeklyShifts(tutorId: tutorId) { shifts in
                    let weekRange = "\(startOfWeek.formatted(date: .abbreviated, time: .omitted)) - \(endOfWeek.formatted(date: .abbreviated, time: .omitted))"

                    guard let pdfURL = viewModel.generateTutorWeeklySchedulePDF(weekRange: weekRange, shifts: shifts) else {
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
            .padding()
            .buttonStyle(.borderedProminent)
            .disabled(viewModel.shiftsForCurrentTutor.isEmpty)
        }
        .navigationTitle("My Schedule")
        .onAppear {
            if let tutorId = Auth.auth().currentUser?.uid {
                viewModel.fetchTutorWeeklyShifts(tutorId: tutorId) { shifts in
                    DispatchQueue.main.async {
                        viewModel.shiftsForCurrentTutor = shifts
                    }
                }
            }
        }
        .refreshable {
            if let tutorId = Auth.auth().currentUser?.uid {
                viewModel.fetchTutorWeeklyShifts(tutorId: tutorId) { shifts in
                    DispatchQueue.main.async {
                        viewModel.shiftsForCurrentTutor = shifts
                    }
                }
            }
        }
    }
}


