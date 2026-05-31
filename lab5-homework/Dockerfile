ARG RAY_VERSION=2.55.1
ARG PY_VERSION=py310
FROM rayproject/ray:${RAY_VERSION}-${PY_VERSION}

RUN pip install --no-cache-dir \
        jupyterlab==4.* \
        ipywidgets \
        nbclient \
    && mkdir -p /home/ray/notebooks

WORKDIR /home/ray
