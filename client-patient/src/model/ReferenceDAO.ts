// Définition d'un Docteur
export interface Doctor {
    id: number;
    lastName: string;
    firstName: string;
    specialtyId: number;
}

// Définition d'une Spécialité
export interface Specialty {
    id: number;
    name: string;
}

export class ReferenceDAO {
    private static BASE_URL = "http://localhost:8080/api";

    // Récupérer tous les docteurs
    async getDoctors(): Promise<Doctor[]> {
        try {
            const response = await fetch(`${ReferenceDAO.BASE_URL}/doctors`);
            return await response.json();
        } catch (e) {
            return [];
        }
    }

    // Récupérer toutes les spécialités
    async getSpecialties(): Promise<Specialty[]> {
        try {
            const response = await fetch(`${ReferenceDAO.BASE_URL}/specialties`);
            return await response.json();
        } catch (e) {
            return [];
        }
    }
}