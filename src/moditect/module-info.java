module com.fasterxml.jackson.dataformat.cbor {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports com.fasterxml.jackson.dataformat.cbor;
    exports com.fasterxml.jackson.dataformat.cbor.databind;

    provides com.fasterxml.jackson.core.JsonFactory with
            com.valaphee.jackson.dataformat.nbt.NbtFactory;
}
