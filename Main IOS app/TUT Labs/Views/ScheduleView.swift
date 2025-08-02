//
//  ScheduleView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import SwiftUI

struct ScheduleView: View {
    @StateObject var viewModel = LabViewModel()

    var body: some View {
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
        .navigationTitle("My Schedule")
        .onAppear {
            viewModel.fetchShiftsForCurrentTutor()
        }
        .refreshable {
            viewModel.fetchShiftsForCurrentTutor()
        }
    }
}

