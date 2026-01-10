export interface Consultation {
    id: number;
    doctorId: number;
    patientId: number | null;
    date: string;
    hour: string;
    reason: string;
}

export class ConsultationDAO {
    private static API_URL = "http://localhost:8080/api/consultations";
    async search(filters: { doctor: string | null, specialty: string | null, date?: string | null }): Promise<Consultation[]> {
        const params = new URLSearchParams()
        if (filters.doctor) params.set('doctor', filters.doctor)
        if (filters.specialty) params.set('specialty', filters.specialty)
        if (filters.date) params.set('date', filters.date)

        const url = `${ConsultationDAO.API_URL}?${params.toString()}`
        const response = await fetch(url)
        if (!response.ok) return []
        return await response.json()
    }

    // 2. CHERCHER PAR PATIENT (Pour "Mes Rendez-vous") <-- NOUVEAU
    async getForPatient(patientId: number): Promise<Consultation[]> {
        try {
            // Selon le PDF, l'API accepte ?patientId=...
            const response = await fetch(`${ConsultationDAO.API_URL}?patientId=${patientId}`);
            if (!response.ok) return [];
            return await response.json();
        } catch (err) {
            console.error(err);
            return [];
        }
    }

    // 3. RÉSERVER
    async book(consultationId: number, patientId: number, reason: string): Promise<boolean> {
        const data = { consultationId, patientId, reason };
        try {
            const response = await fetch(ConsultationDAO.API_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            return response.ok;
        } catch (err) { return false; }
    }

    // 4. ANNULER
    async cancel(id: number): Promise<boolean> {
        try {
            const response = await fetch(`${ConsultationDAO.API_URL}?id=${id}`, { method: 'DELETE' });
            return response.ok;
        } catch (err) { return false; }
    }
}