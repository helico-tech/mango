mod vm;

fn main() {
    let filename = std::env::args().nth(1).expect("Usage : <filename>");

    let mut file = std::fs::File::open(filename).expect("File not found");

    let byte_array = std::io::Read::bytes(&mut file)
        .map(|byte| byte.expect("Error reading file"))
        .collect::<Vec<u8>>();

    let mut vm = vm::VM::new(byte_array);

    vm.run();

    println!("{:?}", vm.get_result());
}