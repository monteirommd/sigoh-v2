import { requisitar } from "./api";
import type { OcupacaoBloco } from "../types/models";

export const OcupacaoService = {
  porBloco: (): Promise<OcupacaoBloco[]> => requisitar<OcupacaoBloco[]>("/api/ocupacao/blocos"),
};
