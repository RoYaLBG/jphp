package php.runtime.loader.dump;

import php.runtime.env.Context;
import php.runtime.env.Environment;
import php.runtime.reflection.FunctionEntity;
import php.runtime.reflection.ParameterEntity;
import php.runtime.loader.dump.io.DumpException;
import php.runtime.loader.dump.io.DumpInputStream;
import php.runtime.loader.dump.io.DumpOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FunctionDumper extends Dumper<FunctionEntity> {
    private ParameterDumper parameterDumper = new ParameterDumper(context, env, debugInformation);

    public FunctionDumper(Context context, Environment env, boolean debugInformation) {
        super(context, env, debugInformation);
    }

    @Override
    public int getType() {
        return Types.FUNCTION;
    }

    @Override
    public void save(FunctionEntity entity, OutputStream output) throws IOException {
        DumpOutputStream data = new DumpOutputStream(output);

        data.writeBoolean(entity.isStatic());

        // name
        data.writeName(entity.getName());
        data.writeName(entity.getInternalName());

        data.writeBoolean(entity.isReturnReference());

        data.writeBoolean(entity.isUsesStackTrace());

        data.writeBoolean(entity.isImmutable());
        data.writeMemory(entity.getImmutableResult());

        data.writeBoolean(entity.isEmpty());

        // trace
        data.writeTrace(debugInformation ? entity.getTrace() : null);

        data.writeInt(entity.parameters == null ? 0 : entity.parameters.length);
        if (entity.parameters != null)
            for(ParameterEntity param : entity.parameters){
                parameterDumper.save(param, output);
            }

        data.writeInt(entity.getData().length);
        data.write(entity.getData());

        data.writeRawData(null);
    }

    @Override
    public FunctionEntity load(InputStream input) throws IOException {
        DumpInputStream data = new DumpInputStream(input);

        FunctionEntity entity = new FunctionEntity(context);
        entity.setStatic(data.readBoolean());
        entity.setName(data.readName());
        entity.setInternalName(data.readName());

        entity.setReturnReference(data.readBoolean());

        entity.setUsesStackTrace(data.readBoolean());

        entity.setImmutable(data.readBoolean());
        entity.setResult(data.readMemory());

        entity.setEmpty(data.readBoolean());
        entity.setTrace(data.readTrace(context));

        int paramCount = data.readInt();
        if (paramCount < 0)
            throw new DumpException("Invalid param count");

        entity.parameters = new ParameterEntity[paramCount];
        for(int i = 0; i < paramCount; i++){
            ParameterEntity param = parameterDumper.load(input);
            param.setTrace(entity.getTrace());
            entity.parameters[i] = param;
        }

        int dataLength = data.readInt();
        byte[] code = new byte[dataLength];
        if (data.read(code) != dataLength)
            throw new DumpException("Cannot read byte-code");

        entity.setData(code);
        data.readRawData();

        return entity;
    }
}
