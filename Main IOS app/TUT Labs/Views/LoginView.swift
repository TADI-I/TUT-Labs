//
//  LoginView.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//

import SwiftUI


struct LoginView: View {
    @ObservedObject var authViewModel: AuthViewModel

    @State private var email = ""
    @State private var password = ""

    var body: some View {
        VStack(spacing: 20) {
            Text("TUT ICT Lab Login")
                .font(.largeTitle)
                .bold()

            TextField("Email", text: $email)
                .textFieldStyle(.roundedBorder)
                .autocapitalization(.none)

            SecureField("Password", text: $password)
                .textFieldStyle(.roundedBorder)

            Button("Login") {
                authViewModel.login(email: email, password: password)
            }
            .buttonStyle(.borderedProminent)

            if !authViewModel.errorMessage.isEmpty {
                Text(authViewModel.errorMessage)
                    .foregroundColor(.red)
                    .padding()
            }
        }
        .padding()
        
    }
      

}
