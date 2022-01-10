import { VectorI } from "../common/declarations";
import { Vector } from "../common/math";

export interface CalcF<T> {
    calc(s: number): T
}