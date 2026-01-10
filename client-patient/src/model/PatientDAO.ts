export class PatientDAO {
    private static API_URL = "http://localhost:8080/api/patients";

    async login(
        lastName: string,
        firstName: string,
        patientId: number | null,
        birthDate: string | null,
        isNew: boolean
    ): Promise<number> {

        const data: any = {
            lastName,
            firstName,
            newPatient: isNew,          // booléen, pas string
        };

        if (isNew) {
            if (!birthDate) throw new Error("birthDate requis pour un nouveau patient");
            data.birthDate = birthDate;
        } else {
            if (patientId === null || Number.isNaN(patientId)) throw new Error("patientId requis");
            data.patientId = patientId; // nombre, pas string
        }

        const response = await fetch(PatientDAO.API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const txt = await response.text(); // ne suppose pas JSON
            try {
                const j = JSON.parse(txt);
                throw new Error(j.error || "Erreur serveur");
            } catch {
                throw new Error(txt || "Erreur serveur");
            }
        }

        const json = await response.json();
        return json.id;
    }
}
