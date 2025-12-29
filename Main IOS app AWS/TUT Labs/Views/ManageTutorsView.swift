//
//  ManageTutorsView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import SwiftUI

struct ManageTutorsView: View {
    @ObservedObject var viewModel: LabViewModel

    @State private var newTutorName = ""
    @State private var newTutorEmail = ""
    
    @State private var showAlert = false
    @State private var alertMessage = ""

    var body: some View {
        Form {
            Section(header: Text("Add New Tutor")) {
                TextField("Name", text: $newTutorName)
                TextField("Email", text: $newTutorEmail)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                
                Button("Add Tutor") {
                    if !isValidEmail(newTutorEmail) {
                        alertMessage = "Please enter a valid email address."
                        showAlert = true
                        return
                    }
                    
                    if viewModel.tutors.contains(where: { $0.email.lowercased() == newTutorEmail.lowercased() }) {
                        alertMessage = "A tutor with this email already exists."
                        showAlert = true
                        return
                    }
                    
                    if newTutorName.trimmingCharacters(in: .whitespaces).isEmpty {
                        alertMessage = "Tutor name cannot be empty."
                        showAlert = true
                        return
                    }

                    viewModel.addTutor(name: newTutorName, email: newTutorEmail)
                    newTutorName = ""
                    newTutorEmail = ""
                }
            }

            Section(header: Text("Current Tutors")) {
                if viewModel.tutors.isEmpty {
                    Text("No tutors found")
                        .foregroundColor(.gray)
                } else {
                    ForEach(viewModel.tutors) { tutor in
                        HStack {
                            VStack(alignment: .leading) {
                                Text(tutor.name)
                                    .font(.headline)
                                Text(tutor.email)
                                    .font(.subheadline)
                                    .foregroundColor(.gray)
                            }
                            Spacer()
                            Button(role: .destructive) {
                                viewModel.removeTutor(tutorId: tutor.id)
                            } label: {
                                Image(systemName: "trash")
                            }
                            .buttonStyle(BorderlessButtonStyle())
                        }
                    }
                }
            }
        }
        .navigationTitle("Manage Tutors")
        .onAppear {
            viewModel.fetchTutors()
        }
        .alert("Validation Error", isPresented: $showAlert) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(alertMessage)
        }
    }

    // MARK: - Email Format Validator
    func isValidEmail(_ email: String) -> Bool {
        let emailFormat =
        #"^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"#
        return NSPredicate(format: "SELF MATCHES %@", emailFormat).evaluate(with: email)
    }
}

struct ManageTutorsView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ManageTutorsView(viewModel: LabViewModel())
        }
    }
}

