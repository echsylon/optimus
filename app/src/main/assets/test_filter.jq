{
  "Text": .text,
  "Bool": .boolean,
  "Float": .number_float,
  "Int": .number_int,
  "Null": .null,
  "Obj": .object | {
    "Len": length,
    "Values": .
  },
  "Arr": .array | {
    "Len": length,
    "Values": .
  }
}
