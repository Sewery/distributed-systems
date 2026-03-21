export interface Astronaut {
    name: string;
    craft: string;
}

export interface Launch {
    id: string;
    name: string;
    window_start: string;
    launch_service_provider: {
        name: string;
    };
    pad: {
        location: {
            name: string;
        };
    },
    rocket:{
        configuration: {
            name: string;
        }
    }
}