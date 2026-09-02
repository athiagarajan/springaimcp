import { Temple } from '../types/temple';

let globalDraggedTemple: Temple | null = null;

export const setGlobalDraggedTemple = (temple: Temple | null) => {
  globalDraggedTemple = temple;
};

export const getGlobalDraggedTemple = (): Temple | null => {
  return globalDraggedTemple;
};
