//
//  LabViewModel.swift
//  TUT Labs
//
//  Created by Tadiwanashe Songore  on 8/2/25.
//
import Foundation
import SwiftUI
import FirebaseFirestore
import FirebaseAuth

class LabViewModel: ObservableObject {
    @Published var tutors: [User] = []
    @Published var labShifts: [LabShift] = []
    @Published var labStatuses: [LabStatus] = []
    @Published var shiftsForCurrentTutor: [LabShift] = []
    
    private var db = Firestore.firestore()
    
    init() {
        fetchTutors()
        fetchShifts()
        fetchLabStatuses()
    }
    
    func fetchLabStatuses() {
        db.collection("labStatus").order(by: "timestamp", descending: true).getDocuments { snapshot, error in
            if let error = error {
                print("Error fetching lab statuses: \(error.localizedDescription)")
                return
            }

            guard let documents = snapshot?.documents else {
                print("No lab statuses found")
                return
            }

            let statuses = documents.compactMap { doc -> LabStatus? in
                let data = doc.data()
                guard let labName = data["labName"] as? String,
                      let isOpen = data["labOpen"] as? Bool,
                      let note = data["note"] as? String,
                      let updatedBy = data["updatedBy"] as? String,
                      let updatedById = data["updatedById"] as? String,
                      let timestamp = data["timestamp"] as? Timestamp else {
                    return nil
                }

                return LabStatus(
                    id: UUID(uuidString: doc.documentID) ?? UUID(),
                    labName: labName,
                    isOpen: isOpen,
                    note: note,
                    updatedBy: updatedBy,
                    updatedById: updatedById,
                    timestamp: timestamp.dateValue()
                )
            }

            DispatchQueue.main.async {
                print("Successfully loaded \(statuses.count) lab statuses")
                self.labStatuses = statuses
            }
        }
    }
    
    // MARK: - Open Lab Session
    func openLabSession(labName: String, note: String = "") {
        print("openLabSession called")
        let db = Firestore.firestore()
        guard let currentUser = Auth.auth().currentUser else {
            print("No logged-in user")
            return
        }

        // Get name from Firestore instead of relying on displayName
        db.collection("users").document(currentUser.uid).getDocument { doc, error in
            let userName = (doc?.data()?["name"] as? String) ?? currentUser.displayName ?? "Unknown"
            let userId = currentUser.uid
            
            let sessionData: [String: Any] = [
                "labName": labName,
                "openedById": userId,
                "openedByName": userName,
                "openedAt": FieldValue.serverTimestamp(),
                "note": note,
                "closedAt": NSNull(),       // 🔹 So we can query for it later
                "closedById": NSNull(),
                "closedByName": NSNull()
            ]
            db.collection("labSessions").addDocument(data: sessionData) { error in
                if let error = error {
                    print("Error opening lab session: \(error.localizedDescription)")
                } else {
                    print("Lab session opened successfully")
                }
            }
        }
    }

    // MARK: - Close Lab Session
    func closeLabSession(labName: String, note: String = "") {
        guard let currentUser = Auth.auth().currentUser else {
            print("No logged-in user")
            return
        }
        
        db.collection("users").document(currentUser.uid).getDocument { doc, error in
            let userName = (doc?.data()?["name"] as? String) ?? currentUser.displayName ?? "Unknown"
            let userId = currentUser.uid
            
            // Find the latest session where closedAt is null
            Firestore.firestore()
                .collection("labSessions")
                .whereField("labName", isEqualTo: labName)
                .whereField("closedAt", isEqualTo: NSNull()) // must exist as null from creation
                .order(by: "openedAt", descending: true)
                .limit(to: 1)
                .getDocuments { snapshot, error in
                    if let error = error {
                        print("Error fetching open lab session: \(error.localizedDescription)")
                        return
                    }
                    
                    guard let doc = snapshot?.documents.first else {
                        print("No open lab session found to close")
                        return
                    }
                    
                    doc.reference.updateData([
                        "closedAt": FieldValue.serverTimestamp(),
                        "closedById": userId,
                        "closedByName": userName,
                        "note": note
                    ]) { error in
                        if let error = error {
                            print("Error closing lab session: \(error.localizedDescription)")
                        } else {
                            print("Lab session closed successfully")
                        }
                    }
                }
        }
    }


        // MARK: - Fetch current open sessions (for display)
        func fetchOpenSessions(completion: @escaping ([LabSession]) -> Void) {
            db.collection("labSessions")
                .whereField("closedAt", isEqualTo: NSNull()) // sessions with no close timestamp = open sessions
                .getDocuments { snapshot, error in
                    if let error = error {
                        print("Error fetching open sessions: \(error.localizedDescription)")
                        completion([])
                        return
                    }

                    let sessions = snapshot?.documents.compactMap { doc -> LabSession? in
                        let data = doc.data()
                        guard
                            let labName = data["labName"] as? String,
                            let openedById = data["openedById"] as? String,
                            let openedByName = data["openedByName"] as? String,
                            let openedAtTimestamp = data["openedAt"] as? Timestamp else {
                            return nil
                        }

                        return LabSession(
                            id: doc.documentID,
                            labName: labName,
                            openedById: openedById,
                            openedByName: openedByName,
                            openedAt: openedAtTimestamp.dateValue(),
                            closedById: nil,
                            closedByName: nil,
                            closedAt: nil,
                            note: data["note"] as? String ?? ""
                        )
                    } ?? []

                    completion(sessions)
                }
        }
    
    // Fetch all tutors with role "tutor"
    func fetchTutors() {
        db.collection("users")
            .whereField("role", isEqualTo: "tutor")
            .getDocuments { snapshot, error in
                if let error = error {
                    print("Error fetching tutors: \(error.localizedDescription)")
                    return
                }
                guard let documents = snapshot?.documents else {
                    print("No tutors found")
                    return
                }
                let fetchedTutors = documents.compactMap { doc -> User? in
                    let data = doc.data()
                    guard
                        let name = data["name"] as? String,
                        let email = data["email"] as? String,
                        let role = data["role"] as? String
                    else {
                        return nil
                    }
                    return User(id: doc.documentID, name: name, email: email, role: role)
                }
                DispatchQueue.main.async {
                    self.tutors = fetchedTutors
                }
            }
    }
    
    // Add a new tutor to Firestore and refresh list
    func addTutor(name: String, email: String) {
        // Generate a default password or set a temporary one
        let tempPassword = "Welcome123!"  // You can generate a stronger one too
        
        Auth.auth().createUser(withEmail: email, password: tempPassword) { result, error in
            if let error = error {
                print("Failed to create auth account: \(error.localizedDescription)")
                return
            }
            
            guard let uid = result?.user.uid else { return }
            
            let tutorData: [String: Any] = [
                "name": name,
                "email": email,
                "role": "tutor"
            ]
            
            self.db.collection("users").document(uid).setData(tutorData) { error in
                if let error = error {
                    print("Failed to save tutor to Firestore: \(error.localizedDescription)")
                } else {
                    print("Tutor added successfully")
                    
                    // Refresh the local list
                    self.fetchTutors()
                }
            }
        }
    }
    
    // Remove a tutor by their document ID and refresh list
    func removeTutor(tutorId: String) {
        db.collection("users").document(tutorId).delete() { error in
            if let error = error {
                print("Error removing tutor: \(error.localizedDescription)")
            } else {
                print("Tutor removed successfully")
                self.fetchTutors() // Refresh list after deleting
            }
        }
    }
    
    // Assign a lab shift to a tutor (save locally + Firestore)
    func assignShift(tutorId: String, day: String, time: String, labName: String) -> Bool {
        let duplicateExists = labShifts.contains { shift in
            shift.labName == labName &&
            shift.day == day &&
            shift.time == time
        }
        
        guard !duplicateExists else {
            return false
        }
        
        let newShift = LabShift(tutorId: tutorId, day: day, time: time, labName: labName)
        labShifts.append(newShift)
        saveShiftToFirestore(shift: newShift)
        return true
    }


    func deleteShift(shift: LabShift) {
        // Remove locally
        if let index = labShifts.firstIndex(where: { $0.id == shift.id }) {
            labShifts.remove(at: index)
        }
        
        // Remove from Firestore
        db.collection("labShifts").document(shift.id.uuidString).delete { error in
            if let error = error {
                print("Error deleting shift: \(error.localizedDescription)")
            } else {
                print("Shift deleted successfully!")
            }
        }
    }

    
    // Save shift document to Firestore with ID = UUID string
    private func saveShiftToFirestore(shift: LabShift) {
        let data: [String: Any] = [
            "tutorId": shift.tutorId,
            "day": shift.day,
            "time": shift.time,
            "labName": shift.labName
        ]
        
        db.collection("labShifts").document(shift.id.uuidString).setData(data) { error in
            if let error = error {
                print("Error writing shift to Firestore: \(error.localizedDescription)")
            } else {
                print("Shift saved successfully!")
            }
        }
    }
    
    func fetchWeeklyLabSessions(startDate: Date, endDate: Date, completion: @escaping ([LabSession]) -> Void) {
        let db = Firestore.firestore()

        db.collection("labSessions")
            .whereField("openedAt", isGreaterThanOrEqualTo: startDate)
            .whereField("openedAt", isLessThanOrEqualTo: endDate)
            .order(by: "openedAt", descending: false)
            .getDocuments { snapshot, error in
                if let error = error {
                    print("Error fetching lab sessions: \(error)")
                    completion([])
                    return
                }

                let sessions: [LabSession] = snapshot?.documents.compactMap { doc in
                    let data = doc.data()
                    guard
                        let labName = data["labName"] as? String,
                        let openedById = data["openedById"] as? String,
                        let openedByName = data["openedByName"] as? String,
                        let openedAtTimestamp = data["openedAt"] as? Timestamp
                    else {
                        return nil
                    }

                    let closedAtTimestamp = data["closedAt"] as? Timestamp
                    let closedAt = closedAtTimestamp?.dateValue()
                    let closedById = data["closedById"] as? String
                    let closedByName = data["closedByName"] as? String
                    let note = data["note"] as? String ?? ""

                    return LabSession(
                        id: doc.documentID,
                        labName: labName,
                        openedById: openedById,
                        openedByName: openedByName,
                        openedAt: openedAtTimestamp.dateValue(),
                        closedById: closedById,
                        closedByName: closedByName,
                        closedAt: closedAt,
                        note: note
                    )
                } ?? []

                completion(sessions)
            }
    }

    func generateWeeklyLabReport(sessions: [LabSession], weekRange: String) -> URL? {
        let pdfMetaData = [
            kCGPDFContextCreator: "TUT Labs System",
            kCGPDFContextTitle: "Weekly Lab Activity Report"
        ]
        let format = UIGraphicsPDFRendererFormat()
        format.documentInfo = pdfMetaData as [String: Any]
        
        let pageWidth = 8.5 * 72.0
        let pageHeight = 11 * 72.0
        let margin: CGFloat = 72
        let contentWidth = pageWidth - margin * 2
        
        let renderer = UIGraphicsPDFRenderer(bounds: CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight), format: format)
        
        let fileName = "Lab_Activity_\(weekRange).pdf"
        let fileURL = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
        
        func drawFooter(in context: UIGraphicsPDFRendererContext) {
            let footerText = "Generated on \(Date().formatted(date: .long, time: .shortened))"
            let attributes: [NSAttributedString.Key: Any] = [
                .font: UIFont.italicSystemFont(ofSize: 10),
                .foregroundColor: UIColor.gray
            ]
            
            let textSize = footerText.size(withAttributes: attributes)
            let textRect = CGRect(x: (pageWidth - textSize.width) / 2,
                                  y: pageHeight - 40,
                                  width: textSize.width,
                                  height: textSize.height)
            
            footerText.draw(in: textRect, withAttributes: attributes)
        }
        
        do {
            try renderer.writePDF(to: fileURL) { context in
                context.beginPage()
                
                // Draw title (add logo if desired here)
                let title = "FoICT Weekly Lab Activity Report (\(weekRange))"
                let titleAttributes: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 18)
                ]
                let titleSize = title.size(withAttributes: titleAttributes)
                let titlePoint = CGPoint(x: margin, y: 60)
                title.draw(at: titlePoint, withAttributes: titleAttributes)
                
                var yPosition = 110.0
                
                let paragraphStyle = NSMutableParagraphStyle()
                paragraphStyle.lineBreakMode = .byWordWrapping
                paragraphStyle.alignment = .left
                
                let attributes: [NSAttributedString.Key: Any] = [
                    .font: UIFont.systemFont(ofSize: 12),
                    .paragraphStyle: paragraphStyle
                ]
                
                for session in sessions {
                    let openedStr = "Opened: \(session.openedAt.formatted(date: .abbreviated, time: .shortened)) by \(session.openedByName)"
                    
                    var closedStr = "Closed: N/A"
                    if let closedAt = session.closedAt, let closedByName = session.closedByName {
                        closedStr = "Closed: \(closedAt.formatted(date: .abbreviated, time: .shortened)) by \(closedByName)"
                    }
                    
                    let noteStr = session.note.isEmpty ? "" : "Note: \(session.note)"
                    
                    let entry = """
                    Lab: \(session.labName)
                    \(openedStr)
                    \(closedStr)
                    \(noteStr)
                    
                    """
                    
                    let attributedText = NSAttributedString(string: entry, attributes: attributes)
                    
                    let textRect = CGRect(x: margin, y: yPosition, width: contentWidth, height: CGFloat.greatestFiniteMagnitude)
                    let textHeight = attributedText.boundingRect(with: CGSize(width: textRect.width, height: CGFloat.greatestFiniteMagnitude),
                                                                 options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil).height
                    
                    // Check for page break and draw footer before new page
                    if yPosition + textHeight > pageHeight - margin {
                        drawFooter(in: context)
                        context.beginPage()
                        yPosition = margin
                    }
                    
                    attributedText.draw(with: CGRect(x: margin, y: yPosition, width: contentWidth, height: textHeight),
                                        options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
                    
                    yPosition += textHeight + 12
                }
                
                // Draw footer on the last page
                drawFooter(in: context)
            }
            
            return fileURL
        } catch {
            print("Could not create PDF: \(error.localizedDescription)")
            return nil
        }
    }



    
    func fetchTutorWeeklyShifts(tutorId: String, completion: @escaping ([LabShift]) -> Void) {
        let db = Firestore.firestore()
        // These can be used for filtering if you have proper date fields
        let startOfWeek = Calendar.current.date(from: Calendar.current.dateComponents([.yearForWeekOfYear, .weekOfYear], from: Date()))!
        let endOfWeek = Calendar.current.date(byAdding: .day, value: 6, to: startOfWeek)!

        db.collection("labShifts")
            .whereField("tutorId", isEqualTo: tutorId)
            .getDocuments { snapshot, error in
                if let error = error {
                    print("Error fetching shifts: \(error.localizedDescription)")
                    completion([])
                    return
                }

                var shifts: [LabShift] = []
                snapshot?.documents.forEach { doc in
                    let data = doc.data()
                    if let day = data["day"] as? String,
                       let labName = data["labName"] as? String,
                       let time = data["time"] as? String,
                       let tutorId = data["tutorId"] as? String {
                        // Optional: filter by week range here if you have a Date field
                        
                        shifts.append(LabShift(tutorId: tutorId, day: day, time: time, labName: labName))
                    }
                }

                completion(shifts)
            }
    }

    func generateTutorWeeklySchedulePDF(weekRange: String, shifts: [LabShift]) -> URL? {
        guard let currentUser = Auth.auth().currentUser else {
            print("No logged-in user")
            return nil
        }
        
        let tutorName = currentUser.displayName ?? "Tutor"
        let pdfMetaData = [
            kCGPDFContextCreator: "TUT Labs System",
            kCGPDFContextTitle: "\(tutorName) - Weekly Schedule"
        ]
        let format = UIGraphicsPDFRendererFormat()
        format.documentInfo = pdfMetaData as [String: Any]
        
        let pageWidth = 8.5 * 72.0
        let pageHeight = 11 * 72.0
        let margin: CGFloat = 50
        let contentWidth = pageWidth - 2 * margin
        
        let renderer = UIGraphicsPDFRenderer(bounds: CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight), format: format)
        
        let safeTutorName = tutorName.replacingOccurrences(of: " ", with: "_")
        let safeFileName = "\(safeTutorName)_Schedule_\(weekRange)".replacingOccurrences(of: " ", with: "_")
        let fileName = "\(safeFileName).pdf"
        let fileURL = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
        
        do {
            try renderer.writePDF(to: fileURL) { context in
                func drawText(_ text: String, at point: CGPoint, font: UIFont, color: UIColor = .black, maxWidth: CGFloat? = nil) -> CGFloat {
                    let paragraphStyle = NSMutableParagraphStyle()
                    paragraphStyle.lineBreakMode = .byWordWrapping
                    paragraphStyle.alignment = .left
                    
                    var attributes: [NSAttributedString.Key: Any] = [
                        .font: font,
                        .foregroundColor: color,
                        .paragraphStyle: paragraphStyle
                    ]
                    
                    let attributedText = NSAttributedString(string: text, attributes: attributes)
                    
                    let drawRect = CGRect(x: point.x, y: point.y, width: maxWidth ?? contentWidth, height: CGFloat.greatestFiniteMagnitude)
                    
                    let textRect = attributedText.boundingRect(with: CGSize(width: drawRect.width, height: CGFloat.greatestFiniteMagnitude),
                                                               options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
                    
                    attributedText.draw(with: drawRect, options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
                    
                    return ceil(textRect.height)
                }
                
                context.beginPage()
                
                // Draw Title Centered
                let title = "FoICT \(tutorName) Weekly Lab Schedule"
                let titleFont = UIFont.boldSystemFont(ofSize: 20)
                let titleSize = title.size(withAttributes: [.font: titleFont])
                let titlePoint = CGPoint(x: (pageWidth - titleSize.width) / 2, y: margin)
                title.draw(at: titlePoint, withAttributes: [.font: titleFont])
                
                // Week Range below title
                let weekFont = UIFont.systemFont(ofSize: 14)
                let weekText = "Week: \(weekRange)"
                let weekSize = weekText.size(withAttributes: [.font: weekFont])
                let weekPoint = CGPoint(x: (pageWidth - weekSize.width) / 2, y: titlePoint.y + titleSize.height )
                weekText.draw(at: weekPoint, withAttributes: [.font: weekFont])
                
                var yPosition = weekPoint.y + weekSize.height + 20
                
                // Draw table headers
                let headerFont = UIFont.boldSystemFont(ofSize: 14)
                let headers = ["Day", "Lab", "Time"]
                let columnWidths: [CGFloat] = [contentWidth * 0.25, contentWidth * 0.5, contentWidth * 0.25]
                
                var xPosition = margin
                for (index, header) in headers.enumerated() {
                    header.draw(at: CGPoint(x: xPosition, y: yPosition), withAttributes: [.font: headerFont])
                    xPosition += columnWidths[index]
                }
                yPosition += 25
                
                // Draw a line below headers
                context.cgContext.setLineWidth(1.0)
                context.cgContext.move(to: CGPoint(x: margin, y: yPosition - 5))
                context.cgContext.addLine(to: CGPoint(x: pageWidth - margin, y: yPosition - 5))
                context.cgContext.strokePath()
                
                // Draw shifts rows
                let rowFont = UIFont.systemFont(ofSize: 12)
                
                for shift in shifts {
                    xPosition = margin
                    let dayHeight = drawText(shift.day, at: CGPoint(x: xPosition, y: yPosition), font: rowFont, maxWidth: columnWidths[0])
                    xPosition += columnWidths[0]
                    let labHeight = drawText(shift.labName, at: CGPoint(x: xPosition, y: yPosition), font: rowFont, maxWidth: columnWidths[1])
                    xPosition += columnWidths[1]
                    let timeHeight = drawText(shift.time, at: CGPoint(x: xPosition, y: yPosition), font: rowFont, maxWidth: columnWidths[2])
                    
                    let maxHeight = max(dayHeight, labHeight, timeHeight)
                    yPosition += maxHeight + 10
                    
                    if yPosition > pageHeight - margin - 40 {
                        context.beginPage()
                        yPosition = margin
                    }
                }
                
                // Footer with generation date and time centered at bottom
                let footer = "Generated: \(Date().formatted(date: .abbreviated, time: .shortened))"
                let footerFont = UIFont.systemFont(ofSize: 10)
                let footerSize = footer.size(withAttributes: [.font: footerFont])
                let footerPoint = CGPoint(x: (pageWidth - footerSize.width) / 2, y: pageHeight - margin)
                footer.draw(at: footerPoint, withAttributes: [.font: footerFont, .foregroundColor: UIColor.gray])
            }
            
            return fileURL
        } catch {
            print("Could not create PDF: \(error.localizedDescription)")
            return nil
        }
    }




    // Fetch all lab shifts
    func fetchShifts() {
        db.collection("labShifts").getDocuments { snapshot, error in
            if let error = error {
                print("Error fetching shifts: \(error.localizedDescription)")
                return
            }
            
            guard let documents = snapshot?.documents else {
                print("No shifts found")
                return
            }
            
            let fetchedShifts = documents.compactMap { doc -> LabShift? in
                let data = doc.data()
                guard
                    let tutorId = data["tutorId"] as? String,
                    let day = data["day"] as? String,
                    let time = data["time"] as? String,
                    let labName = data["labName"] as? String
                else {
                    return nil
                }
                return LabShift(id: UUID(uuidString: doc.documentID) ?? UUID(), tutorId: tutorId, day: day, time: time, labName: labName)
            }
            
            DispatchQueue.main.async {
                self.labShifts = fetchedShifts
            }
        }
    }
    
    // Get shifts filtered for a specific tutor
    func shiftsForTutor(_ tutorId: String) -> [LabShift] {
        labShifts.filter { $0.tutorId == tutorId }
    }
    
    func fetchShiftsForCurrentTutor() {
            guard let uid = Auth.auth().currentUser?.uid else {
                print("No logged-in user")
                self.shiftsForCurrentTutor = []
                return
            }
            print("Fetching shifts for tutorId:", uid)

            db.collection("labShifts")
                .whereField("tutorId", isEqualTo: uid)
                .getDocuments { snapshot, error in
                    if let error = error {
                        print("Error fetching shifts: \(error.localizedDescription)")
                        return
                    }

                    guard let documents = snapshot?.documents else {
                        print("No shifts found")
                        self.shiftsForCurrentTutor = []
                        return
                    }

                    print("Firestore returned \(documents.count) documents")

                    let shifts = documents.compactMap { doc -> LabShift? in
                        let data = doc.data()
                        guard let day = data["day"] as? String,
                              let time = data["time"] as? String,
                              let labName = data["labName"] as? String else {
                            return nil
                        }

                        return LabShift(id: UUID(uuidString: doc.documentID) ?? UUID(),
                                        tutorId: uid,
                                        day: day,
                                        time: time,
                                        labName: labName)
                    }

                    DispatchQueue.main.async {
                        self.shiftsForCurrentTutor = shifts
                    }
                }
        }

}

