path=`pwd`
echo "${path}"

name="pb_message"
echo "${name}"

namelower=$(echo $name | awk '{print tolower($0)}')

protoc --proto_path=${path} --cpp_out=${path}/ios/ ${path}/${name}.proto
protoc --proto_path=${path} --java_out=${path}/java ${path}/${name}.proto