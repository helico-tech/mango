pub struct VM {
    ip: usize,
    code: Vec<u8>,
    stack: Vec<i32>,
}

impl VM {
    pub fn new(code: Vec<u8>) -> VM {
        VM { ip: 0, code, stack: Vec::new() }
    }

    pub fn run(&mut self) {
        while self.ip < self.code.len() {
            self.step();
        }
    }

    pub fn step(&mut self) {
        if self.ip >= self.code.len() {
            return;
        }

        let code = self.code[self.ip];

        self.ip += 1;

        match code {
            0x00 => {
                self.ip = self.code.len();
            },

            0x01 => {
                self.ip = self.stack.pop().expect("Stack underflow") as usize;
            }

            0x02 => {
                let address = self.stack.pop().expect("Stack underflow");
                let result = self.stack.pop().expect("Stack underflow");
                if result == 0 {
                    self.ip = address as usize;
                }
            }

            0x10 => {
                let value = self.get_i32();
                self.stack.push(value);
            }

            0x11 => {
                let offset = self.get_i32();
                let actual_offset = self.stack.len() - offset as usize - 1;
                let value = self.stack[actual_offset];
                self.stack.push(value);

            }

            0x20 => {
                let offset = self.get_i32();
                let value = self.stack.pop().expect("Stack underflow");

                let actual_offset = self.stack.len() - offset as usize - 1;
                self.stack[actual_offset] = value;
            }

            0x21 => {
                let repeats = self.get_i32();
                for _ in 0..repeats {
                    self.stack.pop().expect("Stack underflow");
                }
            }

            0x30 .. 0x35 => {
                let left = self.stack.pop().expect("Stack underflow");
                let right = self.stack.pop().expect("Stack underflow");
                let result = match code {
                    0x30 => left + right,
                    0x31 => left - right,
                    0x32 => left * right,
                    0x33 => left / right,
                    0x34 => left % right,
                    _ => panic!("Unknown opcode"),
                };

                self.stack.push(result);
            }

            0x40 .. 0x45 => {
                let left = self.stack.pop().expect("Stack underflow");
                let right = self.stack.pop().expect("Stack underflow");

                let result = match code {
                    0x40 => left == right,
                    0x41 => left > right,
                    0x42 => left < right,
                    0x43 => left >= right,
                    0x44 => left <= right,
                    _ => panic!("Unknown opcode"),
                };

                self.stack.push(if result { 1 } else { 0 });
            }

            _ => {
                panic!("Unknown opcode");
            }
        }
    }

    pub fn get_result(&self) -> i32 {
        self.stack.last().expect("Stack underflow").clone()
    }

    fn get_i32(&mut self) -> i32 {
        let result = i32::from_be_bytes([
            self.code[self.ip],
            self.code[self.ip + 1],
            self.code[self.ip + 2],
            self.code[self.ip + 3],
        ]);

        self.ip += 4;

        result
    }
}